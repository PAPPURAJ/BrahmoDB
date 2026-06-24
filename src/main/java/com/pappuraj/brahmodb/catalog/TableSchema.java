package com.pappuraj.brahmodb.catalog;

import java.util.List;

public class TableSchema {

    private final String tableName;
    private final List<Column> columns;

    public TableSchema(String tableName, List<Column> columns) {
        this.tableName = tableName;
        this.columns = columns;
    }

    public String getTableName() {
        return tableName;
    }

    public List<Column> getColumns() {
        return columns;
    }
}