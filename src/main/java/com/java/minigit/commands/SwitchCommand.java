package com.java.minigit.commands;

import com.java.minigit.cli.Command;
import com.java.minigit.core.Repository;

import java.util.Optional;

public class SwitchCommand implements Command {
    private final Repository repository;
    private final CheckoutCommand checkoutCommand;

    public SwitchCommand(Repository repository) {
        this.repository = repository;
        this.checkoutCommand = new CheckoutCommand(repository);
    }

    @Override
    public int execute(String[] args) {
        if (!repository.isInitialized()) {
            System.err.println("mygit switch: not a MiniGit repository (or any of the parent directories): .mygit");
            return 1;
        }

        if (args.length == 1) {
            return switchToExistingBranch(args[0]);
        }

        if (args.length == 2 && "-c".equals(args[0])) {
            return createAndSwitch(args[1]);
        }

        System.err.println("Usage: mygit switch <branch> | mygit switch -c <new_branch>");
        return 1;
    }

    private int switchToExistingBranch(String branchName) {
        if (!repository.isValidBranchName(branchName)) {
            System.err.println("mygit switch: invalid branch name: " + branchName);
            return 1;
        }

        if (!repository.branchExists(branchName)) {
            System.err.println("mygit switch: unknown branch: " + branchName);
            return 1;
        }

        return checkoutCommand.execute(new String[]{branchName});
    }

    private int createAndSwitch(String branchName) {
        if (!repository.isValidBranchName(branchName)) {
            System.err.println("mygit switch: invalid branch name: " + branchName);
            return 1;
        }

        if (repository.branchExists(branchName)) {
            System.err.println("mygit switch: branch already exists: " + branchName);
            return 1;
        }

        Optional<String> headCommit = repository.readHeadCommit();
        if (headCommit.isEmpty()) {
            System.err.println("mygit switch: cannot create branch without a commit");
            return 1;
        }

        repository.createBranch(branchName, headCommit.get());
        int switchExit = checkoutCommand.execute(new String[]{branchName});
        if (switchExit == 0) {
            System.out.println("Created and switched to branch " + branchName);
        }
        return switchExit;
    }
}
