package com.biubiu.dms.core.model;

import lombok.Data;

/**
 * @author ：张音乐
 * @date ：Created in 2021/8/10 下午2:11
 * @description：查询外键信息
 * @email: zhangyule1993@sina.com
 * @version:
 */
@Data
public class QueryExportKeys {

    private String pkTableName;

    private String pkColumnName;

    private String fkTableName;

    private String fkColumnName;

    private short updateRule;

    private short delRule;

    private String fkName;

    private String pkName;

}