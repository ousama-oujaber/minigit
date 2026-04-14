package com.java.minigit.commands;

import com.java.minigit.cli.Command;
import com.java.minigit.core.ObjectStore;
import com.java.minigit.core.Repository;
import com.java.minigit.objects.CommitData;
import com.java.minigit.objects.CommitParser;
import com.java.minigit.objects.TreeEntry;
import com.java.minigit.objects.TreeParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class CheckoutCommand implements Command {
    private final Repository repository;
    private final ObjectStore objectStore;

    public CheckoutCommand(Repository repository) {
        this.repository = repository;
        this.objectStore = new ObjectStore(repository.root());
    }

    @Override
    public int execute(String[] args) {
        if (!repository.isInitialized()) {
            System.err.println("mygit checkout: not a MiniGit repository (or any of the parent directories): .mygit");
            return 1;
        }

        if (args.length != 1 || args[0].isBlank()) {
            System.err.println("Usage: mygit checkout <commit_hash|branch>");
            return 1;
        }

        String target = args[0];
        String resolvedCommitHash;
        String targetBranch = null;
        ObjectStore.StoredObject commitObject;

        if (repository.isValidBranchName(target) && repository.branchExists(target)) {
            targetBranch = target;
            Optional<String> branchCommit = repository.readBranchCommit(target);
            if (branchCommit.isEmpty()) {
                System.err.println("mygit checkout: branch has no commit: " + target);
                return 1;
            }
            resolvedCommitHash = branchCommit.get();
        } else {
            try {
                resolvedCommitHash = objectStore.resolveHash(target);
            } catch (IllegalStateException | IllegalArgumentException exception) {
                System.err.println("mygit checkout: invalid commit hash or branch: " + target);
                return 1;
            }
        }

        try {
            commitObject = objectStore.read(resolvedCommitHash);
        } catch (IllegalStateException | IllegalArgumentException exception) {
            System.err.println("mygit checkout: invalid commit hash: " + target);
            return 1;
        }

        if (!"commit".equals(commitObject.type())) {
            System.err.println("mygit checkout: object is not a commit: " + target);
            return 1;
        }

        CommitData commitData = CommitParser.parse(commitObject.contentAsString());
        clearWorkingDirectory();
        restoreTree(commitData.treeHash(), repository.root());

        if (targetBranch != null) {
            repository.updateHeadToBranch(targetBranch);
            System.out.println("Switched to branch " + targetBranch);
        } else {
            repository.updateHeadToCommit(resolvedCommitHash);
            System.out.println("Note: switching to commit " + resolvedCommitHash);
        }
        return 0;
    }

    private void clearWorkingDirectory() {
        try {
            Files.list(repository.root())
                    .filter(path -> !path.getFileName().toString().equals(".mygit"))
                    .forEach(this::deleteRecursively);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to clear working directory", exception);
        }
    }

    private void deleteRecursively(Path path) {
        try {
            if (!Files.exists(path)) {
                return;
            }
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException exception) {
                            throw new IllegalStateException("Unable to delete path: " + p, exception);
                        }
                    });
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to traverse path for deletion: " + path, exception);
        }
    }

    private void restoreTree(String treeHash, Path baseDir) {
        ObjectStore.StoredObject treeObject = objectStore.read(treeHash);
        if (!"tree".equals(treeObject.type())) {
            throw new IllegalStateException("Expected tree object for hash " + treeHash);
        }

        List<TreeEntry> entries = TreeParser.parse(treeObject.contentAsString());
        for (TreeEntry entry : entries) {
            Path target = baseDir.resolve(entry.name());
            if ("tree".equals(entry.type())) {
                createDirectory(target);
                restoreTree(entry.hash(), target);
            } else if ("blob".equals(entry.type())) {
                restoreBlob(entry.hash(), target);
            } else {
                throw new IllegalStateException("Unknown tree entry type: " + entry.type());
            }
        }
    }

    private void createDirectory(Path directory) {
        try {
            Files.createDirectories(directory);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create directory: " + directory, exception);
        }
    }

    private void restoreBlob(String blobHash, Path targetFile) {
        ObjectStore.StoredObject blob = objectStore.read(blobHash);
        if (!"blob".equals(blob.type())) {
            throw new IllegalStateException("Expected blob object for hash " + blobHash);
        }

        try {
            Files.createDirectories(targetFile.getParent());
            Files.writeString(targetFile, blob.contentAsString(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to restore file: " + targetFile, exception);
        }
    }
}
