package com.pappuraj.brahmodb.core;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.pappuraj.brahmodb.catalog.Column;
import com.pappuraj.brahmodb.catalog.TableSchema;
import com.pappuraj.brahmodb.storage.Row;




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




    public TableSchema describeTable(String databaseName, String tableName) {
        if (databaseName == null) {
            return null;
        }

        File schemaFile = new File(
                DATA_DIRECTORY + File.separator + databaseName
                        + File.separator + TABLES_DIRECTORY,
                tableName + ".schema"
        );

        if (!schemaFile.exists()) {
            return null;
        }

        List<Column> columns = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(schemaFile))) {
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] parts = line.split(":");

                if (parts.length == 2) {
                    String columnName = parts[0].trim();
                    String columnType = parts[1].trim();

                    columns.add(new Column(columnName, columnType));
                }
            }
        } catch (IOException e) {
            return null;
        }

        return new TableSchema(tableName, columns);
    }






    public String insertIntoTable(String databaseName, String tableName, Row row) {
        if (databaseName == null) {
            return "No database selected. Use USE database_name; first.";
        }

        TableSchema schema = describeTable(databaseName, tableName);

        if (schema == null) {
            return "Table does not exist: " + tableName;
        }

        if (row.getValues().size() != schema.getColumns().size()) {
            return "Column count does not match value count.";
        }

        String validationError = validateRow(schema, row);

        if (validationError != null) {
            return validationError;
        }

        File dataFile = new File(
                DATA_DIRECTORY + File.separator + databaseName
                        + File.separator + TABLES_DIRECTORY,
                tableName + ".data"
        );

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile, true))) {
            writer.write(String.join("|", row.getValues()));
            writer.newLine();
        } catch (IOException e) {
            return "Failed to insert row: " + e.getMessage();
        }

        return "1 row inserted.";
    }

    private String validateRow(TableSchema schema, Row row) {
        List<Column> columns = schema.getColumns();
        List<String> values = row.getValues();

        for (int i = 0; i < columns.size(); i++) {
            Column column = columns.get(i);
            String value = values.get(i);

            String error = validateValue(column, value);

            if (error != null) {
                return error;
            }
        }

        return null;
    }

    private String validateValue(Column column, String value) {
        String type = column.getType();

        try {
            switch (type) {
                case "INT":
                    Integer.parseInt(value);
                    break;

                case "DOUBLE":
                    Double.parseDouble(value);
                    break;

                case "BOOLEAN":
                    if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                        return "Invalid BOOLEAN value for column '" + column.getName() + "': " + value;
                    }
                    break;

                case "TEXT":
                    if (value.isEmpty()) {
                        return "Invalid TEXT value for column '" + column.getName() + "'.";
                    }
                    break;

                default:
                    return "Unsupported column type: " + type;
            }
        } catch (NumberFormatException e) {
            return "Invalid " + type + " value for column '" + column.getName() + "': " + value;
        }

        return null;
    }






    public List<Row> selectAll(String databaseName, String tableName) {
        List<Row> rows = new ArrayList<>();

        if (databaseName == null) {
            return rows;
        }

        TableSchema schema = describeTable(databaseName, tableName);

        if (schema == null) {
            return null;
        }

        File dataFile = new File(
                DATA_DIRECTORY + File.separator + databaseName
                        + File.separator + TABLES_DIRECTORY,
                tableName + ".data"
        );

        if (!dataFile.exists()) {
            return rows;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", -1);
                List<String> values = new ArrayList<>();

                for (String part : parts) {
                    values.add(part.trim());
                }

                rows.add(new Row(values));
            }
        } catch (IOException e) {
            return rows;
        }

        return rows;
    }





    public List<Row> selectWhere(
            String databaseName,
            String tableName,
            String columnName,
            String expectedValue
    ) {
        List<Row> matchedRows = new ArrayList<>();

        if (databaseName == null) {
            return matchedRows;
        }

        TableSchema schema = describeTable(databaseName, tableName);

        if (schema == null) {
            return null;
        }

        int columnIndex = findColumnIndex(schema, columnName);

        if (columnIndex == -1) {
            return null;
        }

        List<Row> allRows = selectAll(databaseName, tableName);

        if (allRows == null) {
            return null;
        }

        for (Row row : allRows) {
            List<String> values = row.getValues();

            if (columnIndex < values.size()) {
                String actualValue = values.get(columnIndex);

                if (actualValue.equals(expectedValue)) {
                    matchedRows.add(row);
                }
            }
        }

        return matchedRows;
    }

    public int findColumnIndex(TableSchema schema, String columnName) {
        List<Column> columns = schema.getColumns();

        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).getName().equalsIgnoreCase(columnName)) {
                return i;
            }
        }

        return -1;
    }
}