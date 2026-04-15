package com.java.minigit.commands;

import com.java.minigit.cli.Command;
import com.java.minigit.core.Index;
import com.java.minigit.core.Repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StatusCommand implements Command {
    private final Repository repository;

    public StatusCommand(Repository repository) {
        this.repository = repository;
    }

    @Override
    public int execute(String[] args) {
        if (!repository.isInitialized()) {
            System.err.println("mygit status: not a MiniGit repository (or any of the parent directories): .mygit");
            return 1;
        }

        if (args.length != 0) {
            System.err.println("Usage: mygit status");
            return 1;
        }

        String headValue = readHeadValue();
        if (headValue.startsWith("ref: ")) {
            String ref = headValue.substring("ref: ".length());
            int separator = ref.lastIndexOf('/');
            String branch = separator >= 0 ? ref.substring(separator + 1) : ref;
            System.out.println("On branch " + branch);
        } else {
            System.out.println("HEAD detached at " + shortenHash(headValue));
        }

        Optional<String> headCommit = repository.readHeadCommit();
        System.out.println("HEAD commit: " + headCommit.map(this::shortenHash).orElse("(none)"));

        Index index = new Index(repository.indexFile());
        Map<String, String> entries = index.entries();
        if (entries.isEmpty()) {
            System.out.println("Staged files: (none)");
            return 0;
        }

        System.out.println("Staged files:");
        List<String> paths = entries.keySet().stream().sorted().toList();
        for (String path : paths) {
            System.out.println("  " + path);
        }
        return 0;
    }

    private String readHeadValue() {
        try {
            return Files.readString(repository.headFile(), StandardCharsets.UTF_8).trim();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read HEAD", exception);
        }
    }

    private String shortenHash(String hash) {
        return hash.length() > 7 ? hash.substring(0, 7) : hash;
    }
}
