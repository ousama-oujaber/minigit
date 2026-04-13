package com.java.minigit.commands;

import com.java.minigit.core.Index;
import com.java.minigit.core.ObjectStore;
import com.java.minigit.core.Repository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommitCommandTest {
    @TempDir
    Path tempDir;

    @Test
    void commitCreatesCommitObjectAndUpdatesBranchRef() throws Exception {
        Repository repository = new Repository(tempDir);
        new InitCommand(repository).execute(new String[0]);

        Path file = tempDir.resolve("README.txt");
        Files.writeString(file, "hello commit", StandardCharsets.UTF_8);
        new AddCommand(repository).execute(new String[]{"README.txt"});

        CommitCommand commitCommand = new CommitCommand(repository);
        int exitCode = commitCommand.execute(new String[]{"-m", "first commit"});
        assertEquals(0, exitCode);

        String headHash = repository.readCurrentHeadCommit().orElse(null);
        assertNotNull(headHash);

        ObjectStore.StoredObject commitObject = new ObjectStore(tempDir).read(headHash);
        assertEquals("commit", commitObject.type());
        String content = commitObject.contentAsString();
        assertTrue(content.contains("tree "));
        assertTrue(content.contains("author "));
        assertTrue(content.contains("timestamp "));
        assertTrue(content.endsWith("first commit\n"));
    }

    @Test
    void secondCommitContainsParentHash() throws Exception {
        Repository repository = new Repository(tempDir);
        new InitCommand(repository).execute(new String[0]);

        Path file = tempDir.resolve("app.txt");
        Files.writeString(file, "v1", StandardCharsets.UTF_8);
        new AddCommand(repository).execute(new String[]{"app.txt"});
        CommitCommand commitCommand = new CommitCommand(repository);
        commitCommand.execute(new String[]{"-m", "v1"});
        String firstHash = repository.readCurrentHeadCommit().orElseThrow();

        Files.writeString(file, "v2", StandardCharsets.UTF_8);
        new AddCommand(repository).execute(new String[]{"app.txt"});
        commitCommand.execute(new String[]{"-m", "v2"});
        String secondHash = repository.readCurrentHeadCommit().orElseThrow();

        ObjectStore.StoredObject secondCommit = new ObjectStore(tempDir).read(secondHash);
        String content = secondCommit.contentAsString();
        assertTrue(content.contains("parent " + firstHash));

        Index index = new Index(repository.indexFile());
        assertTrue(index.entries().containsKey("app.txt"));
    }

    @Test
    void commitFailsWithEmptyIndex() {
        Repository repository = new Repository(tempDir);
        new InitCommand(repository).execute(new String[0]);

        int exitCode = new CommitCommand(repository).execute(new String[]{"-m", "nothing"});
        assertEquals(1, exitCode);
    }
}
