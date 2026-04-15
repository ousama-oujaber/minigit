package com.java.minigit.cli;

import com.java.minigit.commands.AddCommand;
import com.java.minigit.commands.BranchCommand;
import com.java.minigit.commands.CheckoutCommand;
import com.java.minigit.commands.CommitCommand;
import com.java.minigit.commands.InitCommand;
import com.java.minigit.commands.LogCommand;
import com.java.minigit.commands.StatusCommand;
import com.java.minigit.commands.SwitchCommand;
import com.java.minigit.core.Repository;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class CommandDispatcher {
    private final Map<String, Command> commands;

    public CommandDispatcher(Path repositoryRoot) {
        Repository repository = new Repository(repositoryRoot);
        this.commands = new LinkedHashMap<>();
        this.commands.put("init", new InitCommand(repository));
        this.commands.put("add", new AddCommand(repository));
        this.commands.put("commit", new CommitCommand(repository));
        this.commands.put("log", new LogCommand(repository));
        this.commands.put("checkout", new CheckoutCommand(repository));
        this.commands.put("status", new StatusCommand(repository));
        this.commands.put("branch", new BranchCommand(repository));
        this.commands.put("switch", new SwitchCommand(repository));
    }

    public int dispatch(String[] args) {
        if (args == null || args.length == 0) {
            printUsage();
            return 1;
        }

        String commandName = args[0];
        Command command = commands.get(commandName);
        if (command == null) {
            System.err.println("mygit: unknown command '" + commandName + "'");
            printUsage();
            return 1;
        }

        String[] commandArgs = new String[Math.max(0, args.length - 1)];
        if (commandArgs.length > 0) {
            System.arraycopy(args, 1, commandArgs, 0, commandArgs.length);
        }

        return command.execute(commandArgs);
    }

    private void printUsage() {
        System.out.println("Usage: mygit <command> [args]");
        System.out.println("Commands:");
        System.out.println("  init               Initialize repository in current directory");
        System.out.println("  add <file...>      Add one or more files to index");
        System.out.println("  commit -m <msg>    Create commit from current index");
        System.out.println("  log                Show commit history");
        System.out.println("  checkout <hash>    Restore working tree from commit");
        System.out.println("  status             Show repository status");
        System.out.println("  branch [name]      List or create branches");
        System.out.println("  switch ...         Switch/create-and-switch branches");
    }
}
