
import sqlFormatter from 'sql-formatter';
const isNotNull = (text) => {
    if(text === undefined || text === null || text === '' || text.trim() === '') {
        return false;
    }
    return true;
}

const arrIsNotNull = (arr) => {
    if(arr === undefined || arr === null || !(arr instanceof Array) || arr.length === 0) {
        return false;
    }
    return true;
}

const checkStartWithPrefix = (text, prefix) => {
    return text.trim().startsWith(prefix, 0);
}
/**
 * 检查是否是文本类型
 */
const checkText = (type) => {
    if(['TEXT', 'LONGTEXT', 'VARCHAR', 'CHAR'].includes(type)) {
        return true;
    }
    return false;
}

const open = (text, _that) => {
    _that.$message({
        message: text,
        type: 'warning'
    });
}
const constructSQL = (tableInfo, fieldList, indexList, _that) => {
    // const that = this;
    let sql = "CREATE TABLE `" + tableInfo.tableName + "` (";
    // 构造字段部分
    if(!arrIsNotNull(fieldList)) {
        open('表设计不合理,不符合开发规范, 没有任何字段', _that);
    }
    if((new Set(fieldList.map(ele => { return ele.name })).size !== fieldList.length)) {
        open('存在同名字段, 请遵守开发规范', _that);
        return null;
    }
    for(let index = 0; index < fieldList.length; index++) {
        const item = fieldList[index];
        if(!isNotNull(item.name) || !isNotNull(item.type)) {
            // open('索引类型为空: ' + JSON.stringify(item), _that);
            continue;
        }
        if(!isNotNull(item.comment)) {
            open('字段: ' + item.name + "的表注释为空, 不符合开发规范", _that);
            return null;
        }
        const type = item.type;
        sql = sql + " `"  + item.name + "` ";
        if(type === 'VARCHAR') {
            sql = sql + " VARCHAR(" + item.len + ") ";
        } else if(type === 'DECIMAL') {
            if(item.len === undefined || item.len === null || item.len === '') {
                sql = sql + " DECIMAL(" + item.numPrecRadix  + ", " + item.decimalDigits + ") ";
            } else {
                sql = sql + " DECIMAL(" + item.len  + ", " + item.decimalDigits + ") ";
            }
        } else {
            sql = sql + item.type + " ";
        }
        if(!item.canVoid) {
            sql = sql + " NOT NULL ";
        }
        if(item.defaultValue !== null) {
            sql = sql + " DEFAULT " + item.defaultValue;
        }
        if(type.indexOf('INT') !== -1 && item.autoIncrement) {
            sql = sql + " AUTO_INCREMENT ";
        }
        if(item.dateOnUpdate) {
            sql = sql + " ON UPDATE CURRENT_TIMESTAMP ";
        }
        if(item.comment !== null && item.comment.trim() !== '') {
            sql = sql + " COMMENT '" + item.comment + "'";
        }
        sql = sql + ","
    }
    // 构造索引部分
    // eslint-disable-next-line no-unused-vars
    for(let index = 0; index < indexList.length; index++) {
        const item = indexList[index];
        if(!isNotNull(item.indexName)) {
            open('索引名称不能为空, 如果不再需要该索引, 请删除该行' , _that);
            return null;
        }
        if(!isNotNull(item.indexType)) {
            open('索引类型不能为空, 如果不再需要该索引, 请删除该行' , _that);
            return null;
        }
        if(!arrIsNotNull(item.indexColumns)) {
            open('索引包含列不能为空, 如果不再需要该索引, 请删除该行' , _that);
            return null;
        }
        if(item.indexType === 'Primary') {
            if(isNotNull(item.indexName) && item.indexName.toUpperCase() !== 'PRIMARY') {
                open('主键索引名称只能是[PRIMARY], 系统强制修改', _that);
            }
            const checkList = fieldList.filter((ele) => { return item.indexColumns.includes(ele.name) });
            for(let i = 0; i < checkList.length; i++) {
                const ele = checkList[i];
                if(ele.canVoid) {
                    open('主键索引字段必须不能为空', _that);
                    return null;
                }
            }
            sql = sql + item.indexType + " KEY (" + item.indexColumns.map(ele => {
                if(ele.prefixSize !== '' && ele.prefixSize !== 0) {
                    return "`" + ele.column + "`" +  "(" + ele.prefixSize + ")" ;
                } else {
                    return "`" + ele.column + "`" ;
                }
            }).join(',') + ")";
        }
        if(item.indexType === 'Normal') {
            if(!isNotNull(item.indexName)) {
                open('普通索引名称不能为空', _that);
                return null;
            }
            if(!checkStartWithPrefix(item.indexName, 'idx_')) {
                open('普通索引名称必须以[idx_]开头', _that);
                return null;
            }
            sql = sql + " KEY `" + item.indexName + "` (" + item.indexColumns.map(ele => {
                if(ele.prefixSize !== '' && ele.prefixSize !== 0) {
                    return "`" + ele.column + "`" +  "(" + ele.prefixSize + ")" ;
                } else {
                    return "`" + ele.column + "`" ;
                }
            }).join(',') + ")";
        }
        if(item.indexType === 'Unique') {
            if(!isNotNull(item.indexName)) {
                open('唯一索引名称不能为空', _that);
                return null;
            }
            if(!checkStartWithPrefix(item.indexName, 'uk_')) {
                open('唯一索引名称必须以[uk_]开头', _that);
                return null;
            }
            sql = sql + " UNIQUE KEY `" + item.indexName + "` (" + item.indexColumns.map(ele => {
                if(ele.prefixSize !== '' && ele.prefixSize !== 0) {
                    return "`" + ele.column + "`" +  "(" + ele.prefixSize + ")" ;
                } else {
                    return "`" + ele.column + "`" ;
                }
            }).join(',') + ")";
        }
        if(item.indexType === 'FullText') {
            if(!isNotNull(item.indexName)) {
                open('全文索引名称不能为空', _that);
                return null;
            }
            if(!checkStartWithPrefix(item.indexName, 'idx_')) {
                open('全文索引名称必须以[idx_]开头', _that);
                return null;
            }

            const checkList = fieldList.filter((ele) => { return item.indexColumns.includes(ele.name) });
            for(let i = 0; i < checkList.length; i++) {
                const ele = checkList[i];
                if(!checkText(ele.type)) {
                    open('全文索引字段类型必须是[TEXT, LONGTEXT, VARCHAR, CHAR] 等文本类型', _that);
                    return null;
                }
            }

            sql = sql + " FULLTEXT KEY `" + item.indexName + "` (" + item.indexColumns.map(ele => {
                if(ele.prefixSize !== '' && ele.prefixSize !== 0) {
                    return "`" + ele.column + "`" +  "(" + ele.prefixSize + ")" ;
                } else {
                    return "`" + ele.column + "`" ;
                }
            }).join(',') + ")";
        }
        if(item.indexType === 'Spatial') {
            // 只有 engine = MyISAM 表 才支持
            if(!isNotNull(item.indexName)) {
                open('全文索引名称不能为空', _that);
                return null;
            }
            if(!checkStartWithPrefix(item.indexName, 'idx_')) {
                open('全文索引名称必须以[idx_]开头', _that);
                return null;
            }
            sql = sql + " SPATIAL KEY `" + item.indexName + "` (" + item.indexColumns.map(ele => {
                if(ele.prefixSize !== '' && ele.prefixSize !== 0) {
                    return "`" + ele.column + "`" +  "(" + ele.prefixSize + ")" ;
                } else {
                    return "`" + ele.column + "`" ;
                }
            }).join(',') + ")";
        }
        if(index !== indexList.length - 1) {
            sql = sql + ",";
        }
    }

    sql = sql + ")";
    if(tableInfo.tableCharacter !== null && tableInfo.tableCharacter.trim() !== '') {
        sql = sql + " DEFAULT CHARACTER SET=" + tableInfo.tableCharacter;
    }
    if(tableInfo.tableComment !== null && tableInfo.tableComment.trim() !== '') {
        sql = sql + " COMMENT='" + tableInfo.tableComment + "'";
    }
    return sqlFormatter.format(sql);

}
export default constructSQL;
