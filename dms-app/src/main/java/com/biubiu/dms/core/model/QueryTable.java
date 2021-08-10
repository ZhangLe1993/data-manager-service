package com.biubiu.dms.core.model;

import lombok.Data;

/**
 * @author ：张音乐
 * @date ：Created in 2021/8/10 下午2:36
 * @description：查询表信息
 * @email: zhangyule1993@sina.com
 * @version:
 */
@Data
public class QueryTable {

    public QueryTable() {
    }

    public QueryTable(String name, String type, String comment) {
        this.name = name;
        this.type = type;
        this.comment = comment;
    }

    private String name;
    private String type;
    private String comment;
    private String engine;
    private String charset;
    private Long autoIncrementNum;

}