package com.java.minigit.commands;

import com.java.minigit.cli.Command;
import com.java.minigit.core.Index;
import com.java.minigit.core.ObjectStore;
import com.java.minigit.core.Repository;
import com.java.minigit.objects.CommitObject;
import com.java.minigit.objects.TreeBuilder;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public class CommitCommand implements Command {
    private final Repository repository;
    private final ObjectStore objectStore;

    public CommitCommand(Repository repository) {
        this.repository = repository;
        this.objectStore = new ObjectStore(repository.root());
    }

    @Override
    public int execute(String[] args) {
        if (!repository.isInitialized()) {
            System.err.println("mygit commit: not a MiniGit repository (or any of the parent directories): .mygit");
            return 1;
        }

        if (args.length != 2 || !"-m".equals(args[0]) || args[1].isBlank()) {
            System.err.println("Usage: mygit commit -m <message>");
            return 1;
        }

        String message = args[1];
        Index index = new Index(repository.indexFile());
        Map<String, String> entries = index.entries();
        if (entries.isEmpty()) {
            System.err.println("mygit commit: nothing to commit (index is empty)");
            return 1;
        }

        TreeBuilder treeBuilder = new TreeBuilder(objectStore);
        String rootTreeHash = treeBuilder.buildRootTree(entries);

        Optional<String> parentHash = repository.readCurrentHeadCommit();
        String author = System.getProperty("user.name", "unknown");
        long timestamp = Instant.now().getEpochSecond();

        String commitContent = CommitObject.content(rootTreeHash, parentHash.orElse(null), author, timestamp, message);
        String commitHash = objectStore.save(commitContent, "commit");
        repository.updateCurrentBranch(commitHash);

        System.out.println("[" + repository.currentBranchName() + " " + commitHash.substring(0, 7) + "] " + message);
        return 0;
    }
}
