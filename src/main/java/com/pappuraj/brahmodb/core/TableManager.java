package com.pappuraj.brahmodb.core;

import com.pappuraj.brahmodb.catalog.Column;
import com.pappuraj.brahmodb.catalog.TableSchema;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TableManager {

    private static final String DATA_DIRECTORY = "data";
    private static final String TABLES_DIRECTORY = "tables";

    public String createTable(String databaseName, TableSchema schema) {
        if (databaseName == null) {
            return "No database selected. Use USE database_name; first.";
        }

        if (!isValidName(schema.getTableName())) {
            return "Invalid table name: " + schema.getTableName();
        }

        File databaseDir = new File(DATA_DIRECTORY, databaseName);

        if (!databaseDir.exists()) {
            return "Database does not exist: " + databaseName;
        }

        File tablesDir = new File(databaseDir, TABLES_DIRECTORY);

        if (!tablesDir.exists()) {
            boolean created = tablesDir.mkdirs();

            if (!created) {
                return "Failed to create tables directory.";
            }
        }

        File schemaFile = new File(tablesDir, schema.getTableName() + ".schema");

        if (schemaFile.exists()) {
            return "Table already exists: " + schema.getTableName();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(schemaFile))) {
            writer.write("table=" + schema.getTableName());
            writer.newLine();

            for (Column column : schema.getColumns()) {
                writer.write(column.getName() + ":" + column.getType());
                writer.newLine();
            }
        } catch (IOException e) {
            return "Failed to create table: " + e.getMessage();
        }

        return "Table created: " + schema.getTableName();
    }

    public List<String> showTables(String databaseName) {
        List<String> tables = new ArrayList<>();

        if (databaseName == null) {
            return tables;
        }

        File tablesDir = new File(DATA_DIRECTORY + File.separator + databaseName, TABLES_DIRECTORY);

        if (!tablesDir.exists()) {
            return tables;
        }

        File[] files = tablesDir.listFiles();

        if (files == null) {
            return tables;
        }

        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".schema")) {
                String tableName = file.getName().replace(".schema", "");
                tables.add(tableName);
            }
        }

        return tables;
    }

    private boolean isValidName(String name) {
        return name != null && name.matches("[a-zA-Z_][a-zA-Z0-9_]*");
    }
}