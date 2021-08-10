
import sqlFormatter from 'sql-formatter';

const constructSQL = (tableInfo, fieldList, indexList) => {
    console.log(tableInfo, fieldList, indexList);
    // const that = this;
    let sql = "CREATE TABLE `" + tableInfo.tableName + "` (";
    fieldList.forEach(item => {
        const type = item.type;
        sql = sql + " `"  + item.name + "` ";
        if(type === 'VARCHAR') {
            sql = sql + " VARCHAR(" + item.len + ") ";
        } else if(type === 'DECIMAL') {
            sql = sql + " DECIMAL(" + item.decimalDigits + ", " + item.numPrecRadix+ ") ";
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
        if(item.comment !== null && item.comment.trim() !== '') {
            sql = sql + " COMMENT '" + item.comment + "',";
        }
    });
    // eslint-disable-next-line no-unused-vars
    indexList.forEach((item, index) => {
        if(item.indexType === 'Primary') {
            sql = sql + item.indexType + " KEY (`" + item.indexColumns.join('`,`') + "`)";
        }
        if(index !== indexList.length - 1) {
            sql = sql + ",";
        }
    });
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