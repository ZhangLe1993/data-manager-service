
package com.biubiu.dms.model;

import com.biubiu.dms.core.model.QueryColumn;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DBTables {
    private String dbName;
    private List<QueryColumn> tables = new ArrayList<>(0);

    public DBTables(String dbName) {
        this.dbName = dbName;
    }

    public DBTables() {

    }
}
