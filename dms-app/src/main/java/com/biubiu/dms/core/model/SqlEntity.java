package com.biubiu.dms.core.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SqlEntity {

    //查询sql
    private String sql;

    private Map<String, Object> quaryParams;

    private Map<String, List<String>> authParams;

    public SqlEntity() {
    }

    public SqlEntity(String sql, Map<String, Object> quaryParams, Map<String, List<String>> authParams) {
        this.sql = sql;
        this.quaryParams = quaryParams;
        this.authParams = authParams;
    }
}
