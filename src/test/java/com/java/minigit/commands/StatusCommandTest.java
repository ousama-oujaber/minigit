package com.java.minigit.commands;

import com.java.minigit.core.Repository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatusCommandTest {
    @TempDir
    Path tempDir;

    @Test
    void statusShowsBranchAndStagedFiles() throws Exception {
        Repository repository = new Repository(tempDir);
        new InitCommand(repository).execute(new String[0]);

        Path file = tempDir.resolve("todo.txt");
        Files.writeString(file, "item", StandardCharsets.UTF_8);
        new AddCommand(repository).execute(new String[]{"todo.txt"});

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
        try {
            int exitCode = new StatusCommand(repository).execute(new String[0]);
            assertEquals(0, exitCode);
        } finally {
            System.setOut(original);
        }

        String text = output.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("On branch main"));
        assertTrue(text.contains("HEAD commit: (none)"));
        assertTrue(text.contains("Staged files:"));
        assertTrue(text.contains("  todo.txt"));
    }

    @Test
    void statusShowsDetachedHeadAfterCheckout() throws Exception {
        Repository repository = new Repository(tempDir);
        new InitCommand(repository).execute(new String[0]);

        Path file = tempDir.resolve("app.txt");
        Files.writeString(file, "v1", StandardCharsets.UTF_8);
        new AddCommand(repository).execute(new String[]{"app.txt"});
        new CommitCommand(repository).execute(new String[]{"-m", "v1"});
        String firstHash = repository.readHeadCommit().orElseThrow();

        Files.writeString(file, "v2", StandardCharsets.UTF_8);
        new AddCommand(repository).execute(new String[]{"app.txt"});
        new CommitCommand(repository).execute(new String[]{"-m", "v2"});

        new CheckoutCommand(repository).execute(new String[]{firstHash.substring(0, 8)});

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
        try {
            int exitCode = new StatusCommand(repository).execute(new String[0]);
            assertEquals(0, exitCode);
        } finally {
            System.setOut(original);
        }

        String text = output.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("HEAD detached at "));
        assertTrue(text.contains("HEAD commit: " + firstHash.substring(0, 7)));
    }
}
