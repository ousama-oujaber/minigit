package com.java.minigit.commands;

import com.java.minigit.cli.Command;
import com.java.minigit.core.Repository;

import java.util.List;
import java.util.Optional;

public class BranchCommand implements Command {
    private final Repository repository;

    public BranchCommand(Repository repository) {
        this.repository = repository;
    }

    @Override
    public int execute(String[] args) {
        if (!repository.isInitialized()) {
            System.err.println("mygit branch: not a MiniGit repository (or any of the parent directories): .mygit");
            return 1;
        }

        if (args.length == 0) {
            return listBranches();
        }

        if (args.length == 1) {
            return createBranch(args[0]);
        }

        System.err.println("Usage: mygit branch [branch_name]");
        return 1;
    }

    private int listBranches() {
        List<String> branches = repository.listBranches();
        Optional<String> current = repository.currentBranchNameIfAttached();
        for (String branch : branches) {
            String marker = current.isPresent() && current.get().equals(branch) ? "*" : " ";
            System.out.println(marker + " " + branch);
        }
        return 0;
    }

    private int createBranch(String branchName) {
        if (!repository.isValidBranchName(branchName)) {
            System.err.println("mygit branch: invalid branch name: " + branchName);
            return 1;
        }

        if (repository.branchExists(branchName)) {
            System.err.println("mygit branch: branch already exists: " + branchName);
            return 1;
        }

        Optional<String> headCommit = repository.readHeadCommit();
        if (headCommit.isEmpty()) {
            System.err.println("mygit branch: cannot create branch without a commit");
            return 1;
        }

        repository.createBranch(branchName, headCommit.get());
        System.out.println("Created branch " + branchName);
        return 0;
    }
}
