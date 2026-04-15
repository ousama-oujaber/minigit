package com.java.minigit.commands;

import com.java.minigit.core.Repository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SwitchCommandTest {
    @TempDir
    Path tempDir;

    @Test
    void switchMovesToExistingBranch() throws Exception {
        Repository repository = new Repository(tempDir);
        new InitCommand(repository).execute(new String[0]);

        Path file = tempDir.resolve("file.txt");
        Files.writeString(file, "v1", StandardCharsets.UTF_8);
        new AddCommand(repository).execute(new String[]{"file.txt"});
        new CommitCommand(repository).execute(new String[]{"-m", "v1"});
        new BranchCommand(repository).execute(new String[]{"dev"});

        Files.writeString(file, "v2", StandardCharsets.UTF_8);
        new AddCommand(repository).execute(new String[]{"file.txt"});
        new CommitCommand(repository).execute(new String[]{"-m", "v2"});

        int exitCode = new SwitchCommand(repository).execute(new String[]{"dev"});
        assertEquals(0, exitCode);

        String content = Files.readString(file, StandardCharsets.UTF_8);
        assertEquals("v1", content);

        String head = Files.readString(repository.headFile(), StandardCharsets.UTF_8).trim();
        assertEquals("ref: refs/heads/dev", head);
    }

    @Test
    void switchCreateMakesBranchAndAttachesHead() throws Exception {
        Repository repository = new Repository(tempDir);
        new InitCommand(repository).execute(new String[0]);

        Path file = tempDir.resolve("main.txt");
        Files.writeString(file, "base", StandardCharsets.UTF_8);
        new AddCommand(repository).execute(new String[]{"main.txt"});
        new CommitCommand(repository).execute(new String[]{"-m", "base"});

        int exitCode = new SwitchCommand(repository).execute(new String[]{"-c", "feature/new"});
        assertEquals(0, exitCode);

        assertTrue(repository.branchExists("feature/new"));
        String head = Files.readString(repository.headFile(), StandardCharsets.UTF_8).trim();
        assertEquals("ref: refs/heads/feature/new", head);
    }
}
