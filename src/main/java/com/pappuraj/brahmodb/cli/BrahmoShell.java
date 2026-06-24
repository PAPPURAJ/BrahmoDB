package com.pappuraj.brahmodb.cli;

import com.pappuraj.brahmodb.catalog.Column;
import com.pappuraj.brahmodb.catalog.TableSchema;
import com.pappuraj.brahmodb.core.DatabaseManager;
import com.pappuraj.brahmodb.core.TableManager;
import com.pappuraj.brahmodb.storage.Row;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BrahmoShell {

    private boolean running = true;
    private final DatabaseManager databaseManager;
    private final TableManager tableManager;

    public BrahmoShell() {
        this.databaseManager = new DatabaseManager();
        this.tableManager = new TableManager();
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

            case "show tables;":
            case "show tables":
                handleShowTables();
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
                } else if (normalizedCommand.startsWith("create table ")) {
                    handleCreateTable(command);
                } else if (normalizedCommand.startsWith("describe ")) {
                    handleDescribeTable(command);
                } else if (normalizedCommand.startsWith("desc ")) {
                    handleDescTable(command);
                } else if (normalizedCommand.startsWith("insert into ")) {
                    handleInsertInto(command);
                } else if (normalizedCommand.startsWith("select * from ")) {
                    handleSelectAll(command);
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

    private void handleCreateTable(String command) {
        String currentDatabase = databaseManager.getCurrentDatabase();

        if (currentDatabase == null) {
            System.out.println("No database selected. Use USE database_name; first.");
            return;
        }

        String cleanCommand = command.replace(";", "").trim();

        int tableKeywordEndIndex = cleanCommand.toLowerCase().indexOf("create table") + "create table".length();
        String afterCreateTable = cleanCommand.substring(tableKeywordEndIndex).trim();

        int openParenIndex = afterCreateTable.indexOf("(");
        int closeParenIndex = afterCreateTable.lastIndexOf(")");

        if (openParenIndex == -1 || closeParenIndex == -1 || closeParenIndex < openParenIndex) {
            System.out.println("Invalid CREATE TABLE syntax.");
            System.out.println("Example: CREATE TABLE students (id INT, name TEXT, age INT);");
            return;
        }

        String tableName = afterCreateTable.substring(0, openParenIndex).trim();
        String columnsText = afterCreateTable.substring(openParenIndex + 1, closeParenIndex).trim();

        if (tableName.isEmpty() || columnsText.isEmpty()) {
            System.out.println("Invalid CREATE TABLE syntax.");
            return;
        }

        List<Column> columns = parseColumns(columnsText);

        if (columns.isEmpty()) {
            System.out.println("No valid columns found.");
            return;
        }

        TableSchema schema = new TableSchema(tableName, columns);

        String result = tableManager.createTable(currentDatabase, schema);
        System.out.println(result);
    }

    private void handleInsertInto(String command) {
        String currentDatabase = databaseManager.getCurrentDatabase();

        if (currentDatabase == null) {
            System.out.println("No database selected. Use USE database_name; first.");
            return;
        }

        String cleanCommand = command.replace(";", "").trim();

        String lowerCommand = cleanCommand.toLowerCase();

        int insertIntoIndex = lowerCommand.indexOf("insert into");
        int valuesIndex = lowerCommand.indexOf("values");

        if (insertIntoIndex == -1 || valuesIndex == -1 || valuesIndex <= insertIntoIndex) {
            System.out.println("Invalid INSERT syntax.");
            System.out.println("Example: INSERT INTO students VALUES (1, 'Rahim', 20);");
            return;
        }

        String tableName = cleanCommand
                .substring(insertIntoIndex + "insert into".length(), valuesIndex)
                .trim();

        String valuesPart = cleanCommand
                .substring(valuesIndex + "values".length())
                .trim();

        int openParenIndex = valuesPart.indexOf("(");
        int closeParenIndex = valuesPart.lastIndexOf(")");

        if (tableName.isEmpty() || openParenIndex == -1 || closeParenIndex == -1 || closeParenIndex < openParenIndex) {
            System.out.println("Invalid INSERT syntax.");
            System.out.println("Example: INSERT INTO students VALUES (1, 'Rahim', 20);");
            return;
        }

        String valuesText = valuesPart.substring(openParenIndex + 1, closeParenIndex).trim();

        List<String> values = parseValues(valuesText);

        if (values.isEmpty()) {
            System.out.println("No valid values found.");
            return;
        }

        Row row = new Row(values);

        String result = tableManager.insertIntoTable(currentDatabase, tableName, row);
        System.out.println(result);
    }




    private void handleSelectAll(String command) {
        String currentDatabase = databaseManager.getCurrentDatabase();

        if (currentDatabase == null) {
            System.out.println("No database selected. Use USE database_name; first.");
            return;
        }

        String tableName = command
                .replaceFirst("(?i)select \\* from", "")
                .replace(";", "")
                .trim();

        if (tableName.isEmpty()) {
            System.out.println("Table name is required.");
            return;
        }

        TableSchema schema = tableManager.describeTable(currentDatabase, tableName);

        if (schema == null) {
            System.out.println("Table does not exist: " + tableName);
            return;
        }

        List<Row> rows = tableManager.selectAll(currentDatabase, tableName);

        if (rows == null) {
            System.out.println("Table does not exist: " + tableName);
            return;
        }

        printRows(schema, rows);
    }





    private List<String> parseValues(String valuesText) {
        List<String> values = new ArrayList<>();

        StringBuilder currentValue = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < valuesText.length(); i++) {
            char ch = valuesText.charAt(i);

            if (ch == '\'') {
                insideQuotes = !insideQuotes;
                continue;
            }

            if (ch == ',' && !insideQuotes) {
                values.add(currentValue.toString().trim());
                currentValue.setLength(0);
            } else {
                currentValue.append(ch);
            }
        }

        values.add(currentValue.toString().trim());

        return values;
    }

    private List<Column> parseColumns(String columnsText) {
        List<Column> columns = new ArrayList<>();

        String[] columnDefinitions = columnsText.split(",");

        for (String definition : columnDefinitions) {
            String[] parts = definition.trim().split("\\s+");

            if (parts.length != 2) {
                return new ArrayList<>();
            }

            String columnName = parts[0].trim();
            String columnType = parts[1].trim();

            if (!isValidColumnType(columnType)) {
                return new ArrayList<>();
            }

            columns.add(new Column(columnName, columnType));
        }

        return columns;
    }

    private boolean isValidColumnType(String type) {
        String normalizedType = type.toUpperCase();

        return normalizedType.equals("INT")
                || normalizedType.equals("TEXT")
                || normalizedType.equals("DOUBLE")
                || normalizedType.equals("BOOLEAN");
    }

    private void handleShowTables() {
        String currentDatabase = databaseManager.getCurrentDatabase();

        if (currentDatabase == null) {
            System.out.println("No database selected. Use USE database_name; first.");
            return;
        }

        List<String> tables = tableManager.showTables(currentDatabase);

        if (tables.isEmpty()) {
            System.out.println("No tables found.");
            return;
        }

        System.out.println("+--------------------+");
        System.out.println("| Tables             |");
        System.out.println("+--------------------+");

        for (String table : tables) {
            System.out.printf("| %-18s |%n", table);
        }

        System.out.println("+--------------------+");
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
        System.out.println("  help;                                      Show available commands");
        System.out.println("  version;                                   Show BrahmoDB version");
        System.out.println("  clear;                                     Clear the terminal screen");
        System.out.println("  exit;                                      Exit BrahmoDB shell");
        System.out.println();
        System.out.println("Database commands:");
        System.out.println("  CREATE DATABASE name;                      Create a new database");
        System.out.println("  SHOW DATABASES;                            Show all databases");
        System.out.println("  USE name;                                  Select a database");
        System.out.println();
        System.out.println("Table commands:");
        System.out.println("  CREATE TABLE name (col TYPE, col TYPE);    Create a table");
        System.out.println("  SHOW TABLES;                               Show tables in current database");
        System.out.println("  DESCRIBE table_name;                       Show table structure");
        System.out.println("  DESC table_name;                           Short form of DESCRIBE");
        System.out.println("  INSERT INTO table VALUES (...);            Insert a row into a table");
        System.out.println("  SELECT * FROM table;                       Show all rows from a table");
        System.out.println();
        System.out.println("Supported types:");
        System.out.println("  INT, TEXT, DOUBLE, BOOLEAN");
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

    private void handleDescribeTable(String command) {
        String tableName = command
                .replaceFirst("(?i)describe", "")
                .replace(";", "")
                .trim();

        describeTableByName(tableName);
    }

    private void handleDescTable(String command) {
        String tableName = command
                .replaceFirst("(?i)desc", "")
                .replace(";", "")
                .trim();

        describeTableByName(tableName);
    }

    private void describeTableByName(String tableName) {
        String currentDatabase = databaseManager.getCurrentDatabase();

        if (currentDatabase == null) {
            System.out.println("No database selected. Use USE database_name; first.");
            return;
        }

        if (tableName.isEmpty()) {
            System.out.println("Table name is required.");
            return;
        }

        TableSchema schema = tableManager.describeTable(currentDatabase, tableName);

        if (schema == null) {
            System.out.println("Table does not exist: " + tableName);
            return;
        }

        System.out.println("+--------------------+--------------------+");
        System.out.println("| Column             | Type               |");
        System.out.println("+--------------------+--------------------+");

        for (Column column : schema.getColumns()) {
            System.out.printf("| %-18s | %-18s |%n", column.getName(), column.getType());
        }

        System.out.println("+--------------------+--------------------+");
    }



    private void printRows(TableSchema schema, List<Row> rows) {
        List<Column> columns = schema.getColumns();

        List<Integer> widths = calculateColumnWidths(columns, rows);

        printSeparator(widths);
        printHeader(columns, widths);
        printSeparator(widths);

        for (Row row : rows) {
            printRow(row, widths);
        }

        printSeparator(widths);

        System.out.println(rows.size() + " row(s) selected.");
    }

    private List<Integer> calculateColumnWidths(List<Column> columns, List<Row> rows) {
        List<Integer> widths = new ArrayList<>();

        for (Column column : columns) {
            widths.add(column.getName().length());
        }

        for (Row row : rows) {
            List<String> values = row.getValues();

            for (int i = 0; i < values.size(); i++) {
                int valueLength = values.get(i).length();

                if (valueLength > widths.get(i)) {
                    widths.set(i, valueLength);
                }
            }
        }

        return widths;
    }

    private void printSeparator(List<Integer> widths) {
        System.out.print("+");

        for (Integer width : widths) {
            System.out.print("-".repeat(width + 2));
            System.out.print("+");
        }

        System.out.println();
    }

    private void printHeader(List<Column> columns, List<Integer> widths) {
        System.out.print("|");

        for (int i = 0; i < columns.size(); i++) {
            System.out.print(" " + padRight(columns.get(i).getName(), widths.get(i)) + " |");
        }

        System.out.println();
    }

    private void printRow(Row row, List<Integer> widths) {
        System.out.print("|");

        List<String> values = row.getValues();

        for (int i = 0; i < values.size(); i++) {
            System.out.print(" " + padRight(values.get(i), widths.get(i)) + " |");
        }

        System.out.println();
    }

    private String padRight(String text, int length) {
        return String.format("%-" + length + "s", text);
    }
}