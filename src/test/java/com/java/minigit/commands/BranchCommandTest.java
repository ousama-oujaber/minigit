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

class BranchCommandTest {
    @TempDir
    Path tempDir;

    @Test
    void createsBranchFromHeadCommit() throws Exception {
        Repository repository = new Repository(tempDir);
        new InitCommand(repository).execute(new String[0]);

        Path file = tempDir.resolve("a.txt");
        Files.writeString(file, "v1", StandardCharsets.UTF_8);
        new AddCommand(repository).execute(new String[]{"a.txt"});
        new CommitCommand(repository).execute(new String[]{"-m", "base"});
        String head = repository.readHeadCommit().orElseThrow();

        int exitCode = new BranchCommand(repository).execute(new String[]{"feature/test"});
        assertEquals(0, exitCode);

        String branchHash = repository.readBranchCommit("feature/test").orElseThrow();
        assertEquals(head, branchHash);
    }

    @Test
    void listBranchesMarksCurrentBranch() throws Exception {
        Repository repository = new Repository(tempDir);
        new InitCommand(repository).execute(new String[0]);

        Path file = tempDir.resolve("a.txt");
        Files.writeString(file, "v1", StandardCharsets.UTF_8);
        new AddCommand(repository).execute(new String[]{"a.txt"});
        new CommitCommand(repository).execute(new String[]{"-m", "base"});
        new BranchCommand(repository).execute(new String[]{"dev"});

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
        try {
            int exitCode = new BranchCommand(repository).execute(new String[0]);
            assertEquals(0, exitCode);
        } finally {
            System.setOut(original);
        }

        String text = output.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("* main"));
        assertTrue(text.contains("  dev"));
    }
}
