package com.biubiu.dms.core.model;

import lombok.Data;

/**
 * @author ：张音乐
 * @date ：Created in 2021/8/10 下午1:14
 * @description：表索引信息
 * @email: zhangyule1993@sina.com
 * @version:
 */
@Data
public class QueryIndex {
    private String indexName;
    private short indexType;
    private String indexColumn;

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public void setIndexType(short indexType) {
        this.indexType = indexType;
    }

    public void setIndexColumn(String indexColumn) {
        this.indexColumn = indexColumn;
    }
}