package com.java.minigit;

import com.java.minigit.cli.CommandDispatcher;

import java.nio.file.Path;
import java.nio.file.Paths;

public class App {
    public static void main(String[] args) {
        Path workingDirectory = Paths.get("").toAbsolutePath().normalize();
        CommandDispatcher dispatcher = new CommandDispatcher(workingDirectory);
        int exitCode = dispatcher.dispatch(args);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }
}
