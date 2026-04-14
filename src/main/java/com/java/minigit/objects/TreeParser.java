package com.java.minigit.objects;

import java.util.ArrayList;
import java.util.List;

public final class TreeParser {
    private TreeParser() {
    }

    public static List<TreeEntry> parse(String content) {
        List<TreeEntry> entries = new ArrayList<>();
        if (content == null || content.isBlank()) {
            return entries;
        }

        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }

            String[] parts = line.split(" ", 3);
            if (parts.length != 3) {
                throw new IllegalStateException("Corrupted tree line: " + line);
            }
            entries.add(new TreeEntry(parts[0], parts[1], parts[2]));
        }

        return entries;
    }
}
