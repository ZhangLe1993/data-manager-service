package com.biubiu.dms.core.model;

import lombok.Data;

/**
 * @author ：张音乐
 * @date ：Created in 2021/8/13 下午12:57
 * @description：索引包含列
 * @email: zhangyule1993@sina.com
 * @version:
 */
@Data
public class IndexColumn {
    private String column;
    private String prefixSize;
    private String order;
}