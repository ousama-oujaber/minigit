package com.java.minigit.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class Repository {
    private static final String HEAD_CONTENT = "ref: refs/heads/main\n";

    private final Path root;

    public Repository(Path root) {
        this.root = root;
    }

    public Path root() {
        return root;
    }

    public Path gitDir() {
        return root.resolve(".mygit");
    }

    public Path objectsDir() {
        return gitDir().resolve("objects");
    }

    public Path refsHeadsDir() {
        return gitDir().resolve("refs").resolve("heads");
    }

    public Path headFile() {
        return gitDir().resolve("HEAD");
    }

    public Path indexFile() {
        return gitDir().resolve("index");
    }

    public boolean isInitialized() {
        return Files.isDirectory(gitDir())
                && Files.isDirectory(objectsDir())
                && Files.isDirectory(refsHeadsDir())
                && Files.isRegularFile(headFile());
    }

    public void initialize() {
        try {
            Files.createDirectories(objectsDir());
            Files.createDirectories(refsHeadsDir());
            if (!Files.exists(headFile())) {
                Files.writeString(headFile(), HEAD_CONTENT, StandardCharsets.UTF_8);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to initialize repository", exception);
        }
    }

    public String currentBranchRef() {
        try {
            String head = Files.readString(headFile(), StandardCharsets.UTF_8).trim();
            if (!head.startsWith("ref: ")) {
                throw new IllegalStateException("Detached HEAD is not supported yet");
            }
            return head.substring("ref: ".length());
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read HEAD", exception);
        }
    }

    public String currentBranchName() {
        String ref = currentBranchRef();
        int separator = ref.lastIndexOf('/');
        return separator >= 0 ? ref.substring(separator + 1) : ref;
    }

    public Path resolveRefPath(String ref) {
        return gitDir().resolve(ref);
    }

    public Path branchRefPath(String branchName) {
        return refsHeadsDir().resolve(branchName);
    }

    public boolean isValidBranchName(String branchName) {
        if (branchName == null || branchName.isBlank()) {
            return false;
        }
        if (branchName.contains("..") || branchName.startsWith("/") || branchName.endsWith("/")) {
            return false;
        }
        return branchName.matches("[A-Za-z0-9._/-]+");
    }

    public Optional<String> currentBranchNameIfAttached() {
        try {
            String head = Files.readString(headFile(), StandardCharsets.UTF_8).trim();
            if (!head.startsWith("ref: refs/heads/")) {
                return Optional.empty();
            }
            return Optional.of(head.substring("ref: refs/heads/".length()));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read HEAD", exception);
        }
    }

    public boolean branchExists(String branchName) {
        return Files.isRegularFile(branchRefPath(branchName));
    }

    public Optional<String> readBranchCommit(String branchName) {
        Path refPath = branchRefPath(branchName);
        if (!Files.exists(refPath)) {
            return Optional.empty();
        }

        try {
            String hash = Files.readString(refPath, StandardCharsets.UTF_8).trim();
            return hash.isEmpty() ? Optional.empty() : Optional.of(hash);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read branch ref", exception);
        }
    }

    public List<String> listBranches() {
        try {
            if (!Files.exists(refsHeadsDir())) {
                return List.of();
            }

            try (Stream<Path> stream = Files.walk(refsHeadsDir())) {
                return stream
                        .filter(Files::isRegularFile)
                        .map(path -> refsHeadsDir().relativize(path).toString().replace('\\', '/'))
                        .sorted(Comparator.naturalOrder())
                        .toList();
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to list branches", exception);
        }
    }

    public void createBranch(String branchName, String commitHash) {
        Path refPath = branchRefPath(branchName);
        try {
            Files.createDirectories(refPath.getParent());
            Files.writeString(refPath, commitHash + "\n", StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create branch", exception);
        }
    }

    public Optional<String> readCurrentHeadCommit() {
        Path refPath = resolveRefPath(currentBranchRef());
        if (!Files.exists(refPath)) {
            return Optional.empty();
        }

        try {
            String hash = Files.readString(refPath, StandardCharsets.UTF_8).trim();
            if (hash.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(hash);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read current branch ref", exception);
        }
    }

    public Optional<String> readHeadCommit() {
        try {
            String head = Files.readString(headFile(), StandardCharsets.UTF_8).trim();
            if (head.isEmpty()) {
                return Optional.empty();
            }

            if (head.startsWith("ref: ")) {
                String ref = head.substring("ref: ".length());
                Path refPath = resolveRefPath(ref);
                if (!Files.exists(refPath)) {
                    return Optional.empty();
                }

                String hash = Files.readString(refPath, StandardCharsets.UTF_8).trim();
                return hash.isEmpty() ? Optional.empty() : Optional.of(hash);
            }

            return Optional.of(head);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to resolve HEAD commit", exception);
        }
    }

    public void updateCurrentBranch(String commitHash) {
        Path refPath = resolveRefPath(currentBranchRef());
        try {
            Files.createDirectories(refPath.getParent());
            Files.writeString(refPath, commitHash + "\n", StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to update current branch ref", exception);
        }
    }

    public void updateHeadToCommit(String commitHash) {
        try {
            Files.writeString(headFile(), commitHash + "\n", StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to update HEAD", exception);
        }
    }

    public void updateHeadToBranch(String branchName) {
        try {
            Files.writeString(headFile(), "ref: refs/heads/" + branchName + "\n", StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to update HEAD", exception);
        }
    }
}
