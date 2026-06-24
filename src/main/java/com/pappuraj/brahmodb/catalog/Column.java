package com.pappuraj.brahmodb.catalog;

public class Column {

    private final String name;
    private final String type;

    public Column(String name, String type) {
        this.name = name;
        this.type = type.toUpperCase();
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}