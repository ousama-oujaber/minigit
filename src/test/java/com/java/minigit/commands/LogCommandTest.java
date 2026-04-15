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

class LogCommandTest {
    @TempDir
    Path tempDir;

    @Test
    void logPrintsLatestCommitFirst() throws Exception {
        Repository repository = new Repository(tempDir);
        new InitCommand(repository).execute(new String[0]);

        Path file = tempDir.resolve("story.txt");
        Files.writeString(file, "v1", StandardCharsets.UTF_8);
        new AddCommand(repository).execute(new String[]{"story.txt"});
        new CommitCommand(repository).execute(new String[]{"-m", "first"});

        Files.writeString(file, "v2", StandardCharsets.UTF_8);
        new AddCommand(repository).execute(new String[]{"story.txt"});
        new CommitCommand(repository).execute(new String[]{"-m", "second"});

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
        try {
            int exitCode = new LogCommand(repository).execute(new String[0]);
            assertEquals(0, exitCode);
        } finally {
            System.setOut(original);
        }

        String text = output.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("    second"));
        assertTrue(text.contains("    first"));
        assertTrue(text.indexOf("    second") < text.indexOf("    first"));
    }
}
