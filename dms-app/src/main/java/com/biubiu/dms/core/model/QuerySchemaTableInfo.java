package com.biubiu.dms.core.model;

import lombok.Data;

/**
 * @author ：张音乐
 * @date ：Created in 2021/8/10 下午3:43
 * @description：表引擎信息
 * @email: zhangyule1993@sina.com
 * @version:
 */
@Data
public class QuerySchemaTableInfo {

    private String engine;

    private String character;

    private Long autoIncrementNum;

    public QuerySchemaTableInfo() {
    }

    public QuerySchemaTableInfo(String engine, String character, Long autoIncrementNum) {
        this.engine = engine;
        this.character = character;
        this.autoIncrementNum = autoIncrementNum;
    }
}