package com.biubiu.dms.core.model;

import lombok.Data;

import java.util.List;

/**
 * @author ：张音乐
 * @date ：Created in 2021/8/13 下午12:56
 * @description：索引信息
 * @email: zhangyule1993@sina.com
 * @version:
 */
@Data
public class QueryKey {
    private String indexName;
    private String indexType;
    private List<IndexColumn> indexColumns;
}