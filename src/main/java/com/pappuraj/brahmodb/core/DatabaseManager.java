package com.pappuraj.brahmodb.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static final String DATA_DIRECTORY = "data";

    private String currentDatabase;

    public DatabaseManager() {
        File dataDir = new File(DATA_DIRECTORY);

        if (!dataDir.exists()) {
            boolean created = dataDir.mkdirs();

            if (!created) {
                throw new RuntimeException("Failed to create data directory.");
            }
        }
    }

    public String createDatabase(String databaseName) {
        if (!isValidName(databaseName)) {
            return "Invalid database name: " + databaseName;
        }

        File databaseDir = new File(DATA_DIRECTORY, databaseName);

        if (databaseDir.exists()) {
            return "Database already exists: " + databaseName;
        }

        boolean created = databaseDir.mkdirs();

        if (!created) {
            return "Failed to create database: " + databaseName;
        }

        return "Database created: " + databaseName;
    }

    public List<String> showDatabases() {
        File dataDir = new File(DATA_DIRECTORY);
        File[] files = dataDir.listFiles();

        List<String> databases = new ArrayList<>();

        if (files == null) {
            return databases;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                databases.add(file.getName());
            }
        }

        return databases;
    }

    public String useDatabase(String databaseName) {
        File databaseDir = new File(DATA_DIRECTORY, databaseName);

        if (!databaseDir.exists() || !databaseDir.isDirectory()) {
            return "Database does not exist: " + databaseName;
        }

        currentDatabase = databaseName;

        return "Using database: " + databaseName;
    }

    public String getCurrentDatabase() {
        return currentDatabase;
    }

    private boolean isValidName(String name) {
        return name != null && name.matches("[a-zA-Z_][a-zA-Z0-9_]*");
    }
}