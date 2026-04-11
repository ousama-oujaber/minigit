package com.java.minigit.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ObjectStoreTest {
    @TempDir
    Path tempDir;

    @Test
    void savesAndReadsBlobObject() {
        ObjectStore store = new ObjectStore(tempDir);
        String hash = store.save("hello world", "blob");

        ObjectStore.StoredObject object = store.read(hash);
        assertEquals(hash, object.hash());
        assertEquals("blob", object.type());
        assertEquals("hello world", object.contentAsString());
    }

    @Test
    void storesSameObjectOnlyOnceByHash() {
        ObjectStore store = new ObjectStore(tempDir);
        String hash1 = store.save("same content", "blob");
        String hash2 = store.save("same content", "blob");

        assertEquals(hash1, hash2);

        Path objectPath = tempDir
                .resolve(".mygit")
                .resolve("objects")
                .resolve(hash1.substring(0, 2))
                .resolve(hash1.substring(2));

        assertTrue(objectPath.toFile().exists());
    }

    @Test
    void rejectsInvalidHashOnRead() {
        ObjectStore store = new ObjectStore(tempDir);
        assertThrows(IllegalArgumentException.class, () -> store.read("abc"));
    }

    @Test
    void resolvesUniqueHashPrefix() {
        ObjectStore store = new ObjectStore(tempDir);
        String hash = store.save("prefix target", "blob");

        String resolved = store.resolveHash(hash.substring(0, 8));
        assertEquals(hash, resolved);
    }

    @Test
    void failsOnAmbiguousHashPrefix() throws Exception {
        ObjectStore store = new ObjectStore(tempDir);
        Path dir = tempDir.resolve(".mygit").resolve("objects").resolve("ab");
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("11111111111111111111111111111111111111"), "x");
        Files.writeString(dir.resolve("22222222222222222222222222222222222222"), "y");

        assertThrows(IllegalArgumentException.class, () -> store.resolveHash("ab"));
    }
}
