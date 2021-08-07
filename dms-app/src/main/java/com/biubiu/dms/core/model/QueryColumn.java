package com.biubiu.dms.core.model;


import com.biubiu.dms.core.common.Constants;
import com.biubiu.dms.core.exception.ServerException;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class QueryColumn {
    private String name;
    private String type;
    private String comment;
    private String nullAble;
    private String size;

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

    public void setNullAble(String nullAble) {
        if (StringUtils.isEmpty(nullAble)) {
            throw new ServerException("Empty column nullAble");
        }
        this.nullAble = nullAble;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setType(String type) {
        this.type = type == null ? Constants.EMPTY : type;
    }
}
