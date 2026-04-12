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

class AddCommandTest {
    @TempDir
    Path tempDir;

    @Test
    void addStoresBlobAndStagesFilePath() throws Exception {
        Repository repository = new Repository(tempDir);
        new InitCommand(repository).execute(new String[0]);

        Path file = tempDir.resolve("notes.txt");
        Files.writeString(file, "hello staging", StandardCharsets.UTF_8);

        AddCommand addCommand = new AddCommand(repository);
        int exitCode = addCommand.execute(new String[]{"notes.txt"});

        assertEquals(0, exitCode);

        Index index = new Index(repository.indexFile());
        String hash = index.entries().get("notes.txt");

        ObjectStore.StoredObject storedObject = new ObjectStore(tempDir).read(hash);
        assertEquals("blob", storedObject.type());
        assertEquals("hello staging", storedObject.contentAsString());
    }

    @Test
    void addFailsWhenRepositoryNotInitialized() {
        Repository repository = new Repository(tempDir);
        AddCommand addCommand = new AddCommand(repository);
        int exitCode = addCommand.execute(new String[]{"missing.txt"});

        assertEquals(1, exitCode);
    }
}
