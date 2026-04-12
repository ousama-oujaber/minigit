package com.java.minigit.commands;

import com.java.minigit.core.Repository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InitCommandTest {
    @TempDir
    Path tempDir;

    @Test
    void createsRepositoryStructure() throws Exception {
        Repository repository = new Repository(tempDir);
        InitCommand command = new InitCommand(repository);

        int exitCode = command.execute(new String[0]);

        assertEquals(0, exitCode);
        assertTrue(Files.isDirectory(tempDir.resolve(".mygit")));
        assertTrue(Files.isDirectory(tempDir.resolve(".mygit/objects")));
        assertTrue(Files.isDirectory(tempDir.resolve(".mygit/refs/heads")));

        String head = Files.readString(tempDir.resolve(".mygit/HEAD"), StandardCharsets.UTF_8);
        assertEquals("ref: refs/heads/main\n", head);
    }
}
