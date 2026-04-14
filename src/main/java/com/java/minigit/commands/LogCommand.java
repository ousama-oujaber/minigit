package com.java.minigit.commands;

import com.java.minigit.cli.Command;
import com.java.minigit.core.ObjectStore;
import com.java.minigit.core.Repository;
import com.java.minigit.objects.CommitData;
import com.java.minigit.objects.CommitParser;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class LogCommand implements Command {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final Repository repository;
    private final ObjectStore objectStore;

    public LogCommand(Repository repository) {
        this.repository = repository;
        this.objectStore = new ObjectStore(repository.root());
    }

    @Override
    public int execute(String[] args) {
        if (!repository.isInitialized()) {
            System.err.println("mygit log: not a MiniGit repository (or any of the parent directories): .mygit");
            return 1;
        }

        if (args.length != 0) {
            System.err.println("Usage: mygit log");
            return 1;
        }

        Optional<String> headCommit = repository.readHeadCommit();
        if (headCommit.isEmpty()) {
            return 0;
        }

        String current = headCommit.get();
        while (current != null && !current.isBlank()) {
            ObjectStore.StoredObject object = objectStore.read(current);
            if (!"commit".equals(object.type())) {
                throw new IllegalStateException("Expected commit object for hash " + current);
            }

            CommitData commit = CommitParser.parse(object.contentAsString());
            String formattedDate = DATE_FORMATTER.format(Instant.ofEpochSecond(commit.timestamp()).atOffset(ZoneOffset.UTC));

            System.out.println("commit " + current);
            System.out.println("Author: " + commit.author());
            System.out.println("Date:   " + formattedDate);
            System.out.println();
            System.out.println("    " + commit.message());
            System.out.println();

            current = commit.parentHash();
        }

        return 0;
    }
}
