package com.biubiu.dms.core.model;


import com.biubiu.dms.core.common.Constants;
import com.biubiu.dms.core.exception.ServerException;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class QueryColumn {
    private String name;
    private String type;

    public QueryColumn(String name, String type) {
        if (StringUtils.isEmpty(name)) {
            throw new ServerException("Empty column name");
        }
        if (StringUtils.isEmpty(type)) {
            throw new ServerException("Empty column type, column: " + name);
        }
        this.name = name;
        this.type = type.toUpperCase();
    }

    public void setType(String type) {
        this.type = type == null ? Constants.EMPTY : type;
    }
}
