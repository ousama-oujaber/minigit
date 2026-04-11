package com.java.minigit.utils;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HashUtilsTest {
    @Test
    void computesStableSha1ForString() {
        String hash = HashUtils.sha1Hex("hello");
        assertEquals("aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d", hash);
    }

    @Test
    void computesSameSha1ForEquivalentUtf8Bytes() {
        String fromString = HashUtils.sha1Hex("mini-git");
        String fromBytes = HashUtils.sha1Hex("mini-git".getBytes(StandardCharsets.UTF_8));

        assertEquals(fromString, fromBytes);
    }
}
