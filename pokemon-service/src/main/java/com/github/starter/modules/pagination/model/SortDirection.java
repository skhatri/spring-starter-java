package com.github.starter.modules.pagination.model;

public enum SortDirection {
    ASC("ASC"),
    DESC("DESC");
    
    private final String sqlValue;
    
    SortDirection(String sqlValue) {
        this.sqlValue = sqlValue;
    }
    
    public String toSql() {
        return sqlValue;
    }
    
    public SortDirection reverse() {
        return this == ASC ? DESC : ASC;
    }
} 