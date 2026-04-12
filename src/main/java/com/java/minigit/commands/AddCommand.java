package com.java.minigit.commands;

import com.java.minigit.cli.Command;
import com.java.minigit.core.Index;
import com.java.minigit.core.ObjectStore;
import com.java.minigit.core.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AddCommand implements Command {
    private final Repository repository;
    private final ObjectStore objectStore;

    public AddCommand(Repository repository) {
        this.repository = repository;
        this.objectStore = new ObjectStore(repository.root());
    }

    @Override
    public int execute(String[] args) {
        if (args.length == 0) {
            System.err.println("mygit add: missing file operand");
            return 1;
        }

        if (!repository.isInitialized()) {
            System.err.println("mygit add: not a MiniGit repository (or any of the parent directories): .mygit");
            return 1;
        }

        Index index = new Index(repository.indexFile());
        for (String fileArg : args) {
            Path target = repository.root().resolve(fileArg).normalize();
            if (!Files.exists(target) || Files.isDirectory(target)) {
                System.err.println("mygit add: pathspec '" + fileArg + "' did not match any file");
                return 1;
            }

            if (!target.startsWith(repository.root())) {
                System.err.println("mygit add: refusing to add file outside repository: " + fileArg);
                return 1;
            }

            String relativePath = repository.root().relativize(target).toString().replace('\\', '/');
            try {
                byte[] content = Files.readAllBytes(target);
                String hash = objectStore.save(content, "blob");
                index.stage(relativePath, hash);
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to read file: " + fileArg, exception);
            }
        }

        index.save();
        return 0;
    }
}
