package com.java.minigit.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Index {
    private final Path indexPath;
    private final Map<String, String> entries;

    public Index(Path indexPath) {
        this.indexPath = indexPath;
        this.entries = loadEntries(indexPath);
    }

    public void stage(String filePath, String blobHash) {
        entries.put(filePath, blobHash);
    }

    public Map<String, String> entries() {
        return Collections.unmodifiableMap(entries);
    }

    public void save() {
        List<String> lines = new ArrayList<>();
        entries.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> lines.add(entry.getValue() + "\t" + entry.getKey()));

        try {
            Files.createDirectories(indexPath.getParent());
            Files.write(indexPath, lines, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to persist index file", exception);
        }
    }

    private Map<String, String> loadEntries(Path path) {
        Map<String, String> loaded = new LinkedHashMap<>();
        if (!Files.exists(path)) {
            return loaded;
        }

        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line == null || line.isBlank()) {
                    continue;
                }

                int separatorIndex = line.indexOf('\t');
                if (separatorIndex <= 0 || separatorIndex == line.length() - 1) {
                    throw new IllegalStateException("Corrupted index line: " + line);
                }

                String hash = line.substring(0, separatorIndex);
                String filePath = line.substring(separatorIndex + 1);
                loaded.put(filePath, hash);
            }
            return loaded;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read index file", exception);
        }
    }
}
