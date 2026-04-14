package com.java.minigit.commands;

import com.java.minigit.core.Repository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CheckoutCommandTest {
    @TempDir
    Path tempDir;

    @Test
    void checkoutRestoresEarlierSnapshot() throws Exception {
        Repository repository = new Repository(tempDir);
        new InitCommand(repository).execute(new String[0]);

        Path file = tempDir.resolve("notes.txt");
        Files.writeString(file, "v1", StandardCharsets.UTF_8);
        new AddCommand(repository).execute(new String[]{"notes.txt"});
        new CommitCommand(repository).execute(new String[]{"-m", "v1"});
        String firstHash = repository.readHeadCommit().orElseThrow();

        Files.writeString(file, "v2", StandardCharsets.UTF_8);
        Path extra = tempDir.resolve("extra.tmp");
        Files.writeString(extra, "to be removed", StandardCharsets.UTF_8);
        new AddCommand(repository).execute(new String[]{"notes.txt"});
        new CommitCommand(repository).execute(new String[]{"-m", "v2"});

        int exitCode = new CheckoutCommand(repository).execute(new String[]{firstHash});
        assertEquals(0, exitCode);

        String restored = Files.readString(file, StandardCharsets.UTF_8);
        assertEquals("v1", restored);
        assertFalse(Files.exists(extra));

        String headContent = Files.readString(repository.headFile(), StandardCharsets.UTF_8).trim();
        assertEquals(firstHash, headContent);
    }

    @Test
    void checkoutFailsForUnknownCommit() {
        Repository repository = new Repository(tempDir);
        new InitCommand(repository).execute(new String[0]);

        int exitCode = new CheckoutCommand(repository).execute(new String[]{"deadbeef"});
        assertEquals(1, exitCode);
    }

    @Test
    void checkoutAcceptsAbbreviatedCommitHash() throws Exception {
        Repository repository = new Repository(tempDir);
        new InitCommand(repository).execute(new String[0]);

        Path file = tempDir.resolve("main.txt");
        Files.writeString(file, "alpha", StandardCharsets.UTF_8);
        new AddCommand(repository).execute(new String[]{"main.txt"});
        new CommitCommand(repository).execute(new String[]{"-m", "alpha"});
        String firstHash = repository.readHeadCommit().orElseThrow();

        Files.writeString(file, "beta", StandardCharsets.UTF_8);
        new AddCommand(repository).execute(new String[]{"main.txt"});
        new CommitCommand(repository).execute(new String[]{"-m", "beta"});

        String shortHash = firstHash.substring(0, 8);
        int exitCode = new CheckoutCommand(repository).execute(new String[]{shortHash});
        assertEquals(0, exitCode);

        String restored = Files.readString(file, StandardCharsets.UTF_8);
        assertEquals("alpha", restored);

        String headContent = Files.readString(repository.headFile(), StandardCharsets.UTF_8).trim();
        assertEquals(firstHash, headContent);
    }

    @Test
    void checkoutBranchReattachesHeadAndRestoresBranchSnapshot() throws Exception {
        Repository repository = new Repository(tempDir);
        new InitCommand(repository).execute(new String[0]);

        Path file = tempDir.resolve("data.txt");
        Files.writeString(file, "v1", StandardCharsets.UTF_8);
        new AddCommand(repository).execute(new String[]{"data.txt"});
        new CommitCommand(repository).execute(new String[]{"-m", "v1"});
        new BranchCommand(repository).execute(new String[]{"feature"});

        Files.writeString(file, "v2", StandardCharsets.UTF_8);
        new AddCommand(repository).execute(new String[]{"data.txt"});
        new CommitCommand(repository).execute(new String[]{"-m", "v2"});

        int exitCode = new CheckoutCommand(repository).execute(new String[]{"feature"});
        assertEquals(0, exitCode);

        String restored = Files.readString(file, StandardCharsets.UTF_8);
        assertEquals("v1", restored);

        String headContent = Files.readString(repository.headFile(), StandardCharsets.UTF_8).trim();
        assertEquals("ref: refs/heads/feature", headContent);
    }
}
