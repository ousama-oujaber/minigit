package com.java.minigit.commands;

import com.java.minigit.cli.Command;
import com.java.minigit.core.Repository;

public class InitCommand implements Command {
    private final Repository repository;

    public InitCommand(Repository repository) {
        this.repository = repository;
    }

    @Override
    public int execute(String[] args) {
        if (args.length > 0) {
            System.err.println("mygit init: this command takes no arguments");
            return 1;
        }

        repository.initialize();
        System.out.println("Initialized empty MiniGit repository in " + repository.gitDir());
        return 0;
    }
}
