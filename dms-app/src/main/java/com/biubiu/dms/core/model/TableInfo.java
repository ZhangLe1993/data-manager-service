package com.biubiu.dms.core.model;

import lombok.Data;

import java.util.List;

@Data
public class TableInfo {
    private String tableName;

    private List<String> primaryKeys;

    List<QueryExportKeys> exportedKeys;

    private List<QueryColumn> columns;

    private List<QueryKey> indexs;

    public TableInfo(String tableName, List<String> primaryKeys, List<QueryExportKeys> exportedKeys, List<QueryColumn> columns, List<QueryKey> indexs) {
        this.tableName = tableName;
        this.exportedKeys = exportedKeys;
        this.primaryKeys = primaryKeys;
        this.columns = columns;
        this.indexs = indexs;
    }

    public TableInfo() {
    }
}
