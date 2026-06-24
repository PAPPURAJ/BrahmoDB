package com.pappuraj.brahmodb.cli;

import com.pappuraj.brahmodb.core.DatabaseManager;

import java.util.List;
import java.util.Scanner;

public class BrahmoShell {

    private boolean running = true;
    private final DatabaseManager databaseManager;

    public BrahmoShell() {
        this.databaseManager = new DatabaseManager();
    }

    public void start() {
        printWelcomeMessage();

        Scanner scanner = new Scanner(System.in);

        while (running) {
            printPrompt();

            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            handleCommand(input);
        }

        scanner.close();
    }

    private void handleCommand(String input) {
        String command = input.trim();

        String normalizedCommand = command.toLowerCase();

        switch (normalizedCommand) {
            case "help;":
            case "help":
                printHelp();
                break;

            case "version;":
            case "version":
                printVersion();
                break;

            case "clear;":
            case "clear":
                clearScreen();
                break;

            case "show databases;":
            case "show databases":
                handleShowDatabases();
                break;

            case "exit;":
            case "exit":
            case "quit;":
            case "quit":
                exitShell();
                break;

            default:
                if (normalizedCommand.startsWith("create database ")) {
                    handleCreateDatabase(command);
                } else if (normalizedCommand.startsWith("use ")) {
                    handleUseDatabase(command);
                } else {
                    System.out.println("Unknown command: " + input);
                    System.out.println("Type 'help;' to see available commands.");
                }
        }
    }

    private void handleCreateDatabase(String command) {
        String databaseName = command
                .replaceFirst("(?i)create database", "")
                .replace(";", "")
                .trim();

        if (databaseName.isEmpty()) {
            System.out.println("Database name is required.");
            return;
        }

        String result = databaseManager.createDatabase(databaseName);
        System.out.println(result);
    }

    private void handleShowDatabases() {
        List<String> databases = databaseManager.showDatabases();

        if (databases.isEmpty()) {
            System.out.println("No databases found.");
            return;
        }

        System.out.println("+--------------------+");
        System.out.println("| Databases          |");
        System.out.println("+--------------------+");

        for (String database : databases) {
            System.out.printf("| %-18s |%n", database);
        }

        System.out.println("+--------------------+");
    }

    private void handleUseDatabase(String command) {
        String databaseName = command
                .replaceFirst("(?i)use", "")
                .replace(";", "")
                .trim();

        if (databaseName.isEmpty()) {
            System.out.println("Database name is required.");
            return;
        }

        String result = databaseManager.useDatabase(databaseName);
        System.out.println(result);
    }

    private void printPrompt() {
        String currentDatabase = databaseManager.getCurrentDatabase();

        if (currentDatabase == null) {
            System.out.print("brahmodb> ");
        } else {
            System.out.print("brahmodb[" + currentDatabase + "]> ");
        }
    }

    private void printWelcomeMessage() {
        System.out.println("======================================");
        System.out.println("        Welcome to BrahmoDB");
        System.out.println(" Oracle-inspired DB engine in Java");
        System.out.println("======================================");
        System.out.println("Type 'help;' for available commands.");
        System.out.println();
    }

    private void printHelp() {
        System.out.println("Available commands:");
        System.out.println("  help;                  Show available commands");
        System.out.println("  version;               Show BrahmoDB version");
        System.out.println("  clear;                 Clear the terminal screen");
        System.out.println("  exit;                  Exit BrahmoDB shell");
        System.out.println();
        System.out.println("Database commands:");
        System.out.println("  CREATE DATABASE name;  Create a new database");
        System.out.println("  SHOW DATABASES;        Show all databases");
        System.out.println("  USE name;              Select a database");
    }

    private void printVersion() {
        System.out.println("BrahmoDB version 1.0.0");
        System.out.println("Java-based relational database engine from scratch.");
    }

    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private void exitShell() {
        System.out.println("Exiting BrahmoDB. Goodbye.");
        running = false;
    }
}