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

    public QueryTable(String name, String type, String comment, String engine, String charset, String autoIncrementNum) {
        this.name = name;
        this.type = type;
        this.comment = comment;
        this.engine = engine;
        this.charset = charset;
        this.autoIncrementNum = autoIncrementNum;
    }

    private String name;
    private String type;
    private String comment;
    private String engine;
    private String charset;
    private String autoIncrementNum;

}