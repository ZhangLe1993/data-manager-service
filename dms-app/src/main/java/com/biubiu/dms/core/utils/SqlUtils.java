package com.biubiu.dms.core.utils;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.util.StringUtils;
import com.biubiu.dms.core.consts.Consts;
import com.biubiu.dms.core.enums.DataTypeEnum;
import com.biubiu.dms.core.enums.SqlColumnEnum;
import com.biubiu.dms.core.enums.SqlTypeEnum;
import com.biubiu.dms.core.exception.ServerException;
import com.biubiu.dms.core.exception.SourceException;
import com.biubiu.dms.core.jdbc.JdbcDataSource;
import com.biubiu.dms.core.model.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.biubiu.dms.core.consts.Consts.*;
import static com.biubiu.dms.core.enums.DataTypeEnum.*;


@Slf4j
@Component
@Scope("prototype")
public class SqlUtils {
    private static final Logger sqlLogger = LoggerFactory.getLogger("BUSINESS_SQL");

    @Autowired
    private JdbcDataSource jdbcDataSource;

    @Value("${source.result-limit:1000000}")
    private int resultLimit;

    @Value("${source.enable-query-log:false}")
    private boolean isQueryLogEnable;

    private static final String TABLE = "TABLE";

    private static final String VIEW = "VIEW";

    private static final String[] TABLE_TYPES = new String[]{TABLE, VIEW};

    private static final String TABLE_NAME = "TABLE_NAME";

    private static final String TABLE_TYPE = "TABLE_TYPE";

    private JdbcSourceInfo jdbcSourceInfo;

    @Getter
    private DataTypeEnum dataTypeEnum;

    private SourceUtils sourceUtils;

    private BaseSource source;

    public SqlUtils init(BaseSource source) {
        SqlUtils sqlUtils = SqlUtilsBuilder
                .getBuilder()
                .withJdbcUrl(source.getJdbcUrl())
                .withUsername(source.getUsername())
                .withPassword(source.getPassword())
                .withDbVersion(source.getDbVersion())
                .withProperties(source.getProperties())
                .withIsExt(source.isExt())
                .withJdbcDataSource(this.jdbcDataSource)
                .withResultLimit(this.resultLimit)
                .withIsQueryLogEnable(this.isQueryLogEnable)
                .build();
        sqlUtils.source = source;
        return sqlUtils;
    }

    public SqlUtils init(String jdbcUrl, String username, String password, String dbVersion, List<Dict> properties, boolean ext) {
        return SqlUtilsBuilder
                .getBuilder()
                .withJdbcUrl(jdbcUrl)
                .withUsername(username)
                .withPassword(password)
                .withDbVersion(dbVersion)
                .withProperties(properties)
                .withIsExt(ext)
                .withJdbcDataSource(this.jdbcDataSource)
                .withResultLimit(this.resultLimit)
                .withIsQueryLogEnable(this.isQueryLogEnable)
                .build();
    }

    public void execute(String sql) throws ServerException {
        sql = filterAnnotate(sql);
        // 关闭敏感SQL检查
        // checkSensitiveSql(sql);
        if (isQueryLogEnable) {
            String md5 = MD5Util.getMD5(sql, true, 16);
            sqlLogger.info("{} execute for sql:{}", md5, formatSql(sql));
        }
        try {
            jdbcTemplate().execute(sql);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ServerException(e.getMessage());
        }
    }

    public void executeSensitive(String sql) throws ServerException {
        sql = filterAnnotate(sql);
        if (isQueryLogEnable) {
            String md5 = MD5Util.getMD5(sql, true, 16);
            sqlLogger.info("{} execute for sql:{}", md5, formatSql(sql));
        }
        try {
            jdbcTemplate().execute(sql);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ServerException(e.getMessage());
        }
    }

    @Cacheable(value = "query", keyGenerator = "keyGenerator", sync = true)
    public PaginateWithQueryColumns syncQuery4Paginate(String sql, Integer pageNo, Integer pageSize, Integer totalCount, Integer limit, Set<String> excludeColumns) throws Exception {
        if (null == pageNo || pageNo < 1) {
            pageNo = 0;
        }
        if (null == pageSize || pageSize < 1) {
            pageSize = 0;
        }
        if (null == totalCount || totalCount < 1) {
            totalCount = 0;
        }
        if (null == limit) {
            limit = -1;
        }
        PaginateWithQueryColumns paginate = query4Paginate(sql, pageNo, pageSize, totalCount, limit, excludeColumns);
        return paginate;
    }

    @CachePut(value = "query", key = "#sql")
    public List<Map<String, Object>> query4List(String sql, int limit) throws Exception {
        sql = filterAnnotate(sql);
        checkSensitiveSql(sql);
        JdbcTemplate jdbcTemplate = jdbcTemplate();
        jdbcTemplate.setMaxRows(limit > resultLimit ? resultLimit : limit);

        long before = System.currentTimeMillis();

        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);

        if (isQueryLogEnable) {
            String md5 = MD5Util.getMD5(sql, true, 16);
            sqlLogger.info("{} query for({} ms) total count: {} sql:{}", md5, System.currentTimeMillis() - before, list.size(), formatSql(sql));
        }

        return list;
    }

    @CachePut(value = "query", keyGenerator = "keyGenerator")
    public PaginateWithQueryColumns query4Paginate(String sql, int pageNo, int pageSize, int totalCount, int limit, Set<String> excludeColumns) throws Exception {
        PaginateWithQueryColumns paginateWithQueryColumns = new PaginateWithQueryColumns();
        sql = filterAnnotate(sql);
        checkSensitiveSql(sql);

        long before = System.currentTimeMillis();

        JdbcTemplate jdbcTemplate = jdbcTemplate();
        jdbcTemplate.setMaxRows(resultLimit);

        if (pageNo < 1 && pageSize < 1) {

            if (limit > 0) {
                resultLimit = limit > resultLimit ? resultLimit : limit;
            }

            jdbcTemplate.setMaxRows(resultLimit);

            // special for mysql
            if (getDataTypeEnum() == MYSQL) {
                jdbcTemplate.setFetchSize(Integer.MIN_VALUE);
            }

            getResultForPaginate(sql, paginateWithQueryColumns, jdbcTemplate, excludeColumns, -1);
            paginateWithQueryColumns.setPageNo(1);
            int size = paginateWithQueryColumns.getResultList().size();
            paginateWithQueryColumns.setPageSize(size);
            paginateWithQueryColumns.setTotalCount(size);

        } else {
            paginateWithQueryColumns.setPageNo(pageNo);
            paginateWithQueryColumns.setPageSize(pageSize);

            int startRow = (pageNo - 1) * pageSize;

            if (pageNo == 1 || totalCount == 0) {
                Object o = jdbcTemplate.queryForList(getCountSql(sql), Object.class).get(0);
                totalCount = Integer.parseInt(String.valueOf(o));
            }

            if (limit > 0) {
                limit = limit > resultLimit ? resultLimit : limit;
                totalCount = Math.min(limit, totalCount);
            }

            paginateWithQueryColumns.setTotalCount(totalCount);
            int maxRows = limit > 0 && limit < pageSize * pageNo ? limit : pageSize * pageNo;

            if (this.dataTypeEnum == MYSQL) {
                sql = sql + " LIMIT " + startRow + ", " + pageSize;
                getResultForPaginate(sql, paginateWithQueryColumns, jdbcTemplate, excludeColumns, -1);
            } else {
                jdbcTemplate.setMaxRows(maxRows);
                getResultForPaginate(sql, paginateWithQueryColumns, jdbcTemplate, excludeColumns, startRow);
            }
        }

        if (isQueryLogEnable) {
            String md5 = MD5Util.getMD5(sql + pageNo + pageSize + limit, true, 16);
            sqlLogger.info("{} query for({} ms) total count: {}, page size: {}, sql:{}",
                    md5, System.currentTimeMillis() - before,
                    paginateWithQueryColumns.getTotalCount(),
                    paginateWithQueryColumns.getPageSize(),
                    formatSql(sql));
        }

        return paginateWithQueryColumns;
    }

    private void getResultForPaginate(String sql, PaginateWithQueryColumns paginateWithQueryColumns, JdbcTemplate jdbcTemplate, Set<String> excludeColumns, int startRow) {
        Set<String> queryFromsAndJoins = getQueryFromsAndJoins(sql);
        jdbcTemplate.query(sql, rs -> {
            if (null == rs) {
                return paginateWithQueryColumns;
            }

            ResultSetMetaData metaData = rs.getMetaData();
            List<QueryColumn> queryColumns = new ArrayList<>();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                String key = getColumnLabel(queryFromsAndJoins, metaData.getColumnLabel(i));
                if (!CollectionUtils.isEmpty(excludeColumns) && excludeColumns.contains(key)) {
                    continue;
                }
                queryColumns.add(new QueryColumn(key, metaData.getColumnTypeName(i)));
            }
            paginateWithQueryColumns.setColumns(queryColumns);

            List<Map<String, Object>> resultList = new ArrayList<>();

            try {
                if (startRow > 0) {
                    rs.absolute(startRow);
                }
                while (rs.next()) {
                    resultList.add(getResultObjectMap(excludeColumns, rs, metaData, queryFromsAndJoins));
                }
            } catch (Throwable e) {
                int currentRow = 0;
                while (rs.next()) {
                    if (currentRow >= startRow) {
                        resultList.add(getResultObjectMap(excludeColumns, rs, metaData, queryFromsAndJoins));
                    }
                    currentRow++;
                }
            }

            paginateWithQueryColumns.setResultList(resultList);

            return paginateWithQueryColumns;
        });
    }

    private Map<String, Object> getResultObjectMap(Set<String> excludeColumns, ResultSet rs, ResultSetMetaData metaData, Set<String> queryFromsAndJoins) throws SQLException {
        Map<String, Object> map = new LinkedHashMap<>();

        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String key = metaData.getColumnLabel(i);
            String label = getColumnLabel(queryFromsAndJoins, key);

            if (!CollectionUtils.isEmpty(excludeColumns) && excludeColumns.contains(label)) {
                continue;
            }
            Object value = rs.getObject(key);
            map.put(label, value instanceof byte[] ? new String((byte[]) value) : value);
        }
        return map;
    }

    public static String getCountSql(String sql) {
        String countSql = String.format(Consts.QUERY_COUNT_SQL, sql);
        try {
            Select select = (Select) CCJSqlParserUtil.parse(sql);
            PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
            plainSelect.setOrderByElements(null);
            countSql = String.format(Consts.QUERY_COUNT_SQL, select.toString());
        } catch (JSQLParserException e) {
            log.debug(e.getMessage(), e);
        }
        return rebuildSqlWithFragment(countSql);
    }

    private static final String WITH = "with";

    public static String rebuildSqlWithFragment(String sql) {
        if (!sql.toLowerCase().startsWith(WITH)) {
            Matcher matcher = WITH_SQL_FRAGMENT.matcher(sql);
            if (matcher.find()) {
                String withFragment = matcher.group();
                if (!org.apache.commons.lang3.StringUtils.isEmpty(withFragment)) {
                    if (withFragment.length() > 6) {
                        int lastSelectIndex = withFragment.length() - 6;
                        sql = sql.replace(withFragment, withFragment.substring(lastSelectIndex));
                        withFragment = withFragment.substring(0, lastSelectIndex);
                    }
                    sql = withFragment + SPACE + sql;
                    sql = sql.replaceAll(SPACE + "{2,}", SPACE);
                }
            }
        }
        return sql;
    }

    public static boolean isSelect(String src) {
        if (StringUtils.isEmpty(src)) {
            return false;
        }
        try {
            Statement parse = CCJSqlParserUtil.parse(src);
            return parse instanceof Select;
        } catch (JSQLParserException e) {
            return false;
        }
    }

    public static Set<String> getQueryFromsAndJoins(String sql) {
        Set<String> columnPrefixs = new HashSet<>();
        try {
            Statement parse = CCJSqlParserUtil.parse(sql);
            Select select = (Select) parse;
            SelectBody selectBody = select.getSelectBody();
            if (selectBody instanceof PlainSelect) {
                PlainSelect plainSelect = (PlainSelect) selectBody;
                columnPrefixExtractor(columnPrefixs, plainSelect);
            }

            if (selectBody instanceof SetOperationList) {
                SetOperationList setOperationList = (SetOperationList) selectBody;
                List<SelectBody> selects = setOperationList.getSelects();
                for (SelectBody optSelectBody : selects) {
                    PlainSelect plainSelect = (PlainSelect) optSelectBody;
                    columnPrefixExtractor(columnPrefixs, plainSelect);
                }
            }

            if (selectBody instanceof WithItem) {
                WithItem withItem = (WithItem) selectBody;
                PlainSelect plainSelect = (PlainSelect) withItem.getSelectBody();
                columnPrefixExtractor(columnPrefixs, plainSelect);
            }
        } catch (JSQLParserException e) {
            log.debug(e.getMessage(), e);
        }
        return columnPrefixs;
    }

    private static void columnPrefixExtractor(Set<String> columnPrefixs, PlainSelect plainSelect) {
        getFromItemName(columnPrefixs, plainSelect.getFromItem());
        List<Join> joins = plainSelect.getJoins();
        if (!CollectionUtils.isEmpty(joins)) {
            joins.forEach(join -> getFromItemName(columnPrefixs, join.getRightItem()));
        }
    }

    private static void getFromItemName(Set<String> columnPrefixs, FromItem fromItem) {
        if (fromItem == null) {
            return;
        }
        Alias alias = fromItem.getAlias();
        if (alias != null) {
            if (alias.isUseAs()) {
                columnPrefixs.add(alias.getName().trim() + DOT);
            } else {
                columnPrefixs.add(alias.toString().trim() + DOT);
            }
        } else {
            fromItem.accept(getFromItemTableName(columnPrefixs));
        }
    }

    public static String getColumnLabel(Set<String> columnPrefixs, String columnLable) {
        if (!CollectionUtils.isEmpty(columnPrefixs)) {
            for (String prefix : columnPrefixs) {
                if (columnLable.startsWith(prefix)) {
                    return columnLable.replaceFirst(prefix, EMPTY);
                }
                if (columnLable.startsWith(prefix.toLowerCase())) {
                    return columnLable.replaceFirst(prefix.toLowerCase(), EMPTY);
                }
                if (columnLable.startsWith(prefix.toUpperCase())) {
                    return columnLable.replaceFirst(prefix.toUpperCase(), EMPTY);
                }
            }
        }
        return columnLable;
    }

    /**
     * 获取当前连接数据库
     *
     * @return
     * @throws
     */
    public List<String> getDatabases() throws SourceException {
        List<String> dbList = new ArrayList<>();
        Connection connection = null;
        try {
            connection = sourceUtils.getConnection(this.jdbcSourceInfo);
            if (null == connection) {
                return dbList;
            }

            if (dataTypeEnum == ORACLE) {
                dbList.add(this.jdbcSourceInfo.getUsername());
                return dbList;
            }

            if (dataTypeEnum == ELASTICSEARCH) {
                if (StringUtils.isEmpty(this.jdbcSourceInfo.getUsername())) {
                    dbList.add(dataTypeEnum.getFeature());
                } else {
                    dbList.add(this.jdbcSourceInfo.getUsername());
                }
                return dbList;
            }

            String catalog = connection.getCatalog();
            if (!StringUtils.isEmpty(catalog)) {
                dbList.add(catalog);
            } else {
                DatabaseMetaData metaData = connection.getMetaData();
                ResultSet rs = metaData.getCatalogs();
                while (rs.next()) {
                    dbList.add(rs.getString(1));
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return dbList;
        } finally {
            SourceUtils.releaseConnection(connection);
        }
        return dbList;
    }

    /**
     * 获取当前数据源表结构
     *
     * @return
     * @throws
     */
    public List<QueryTable> getTableList(String dbName) throws SourceException {
        List<QueryTable> tableList = null;
        Connection connection = null;
        ResultSet rs = null;
        try {
            connection = sourceUtils.getConnection(this.jdbcSourceInfo);
            if (null == connection) {
                return tableList;
            }
            DatabaseMetaData metaData = connection.getMetaData();
            String schema = null;
            try {
                schema = metaData.getConnection().getSchema();
            } catch (Throwable t) {
                // ignore
            }
            rs = metaData.getTables(dbName, getDBSchemaPattern(schema), "%", TABLE_TYPES);
            if (null == rs) {
                return tableList;
            }
            tableList = new ArrayList<>();
            while (rs.next()) {
                String name = rs.getString(TABLE_NAME);
                if (!StringUtils.isEmpty(name)) {
                    String type = TABLE;
                    String remarks = "";
                    String engine = "";
                    String charset = "";
                    String autoIncrementNum = "";
                    try {
                        type = rs.getString(TABLE_TYPE);
                        remarks = rs.getString("REMARKS");
                        engine = rs.getString("ENGINE");
                        charset = rs.getString("CHARACTER_SET_NAME");
                        autoIncrementNum = rs.getString("AUTO_INCREMENT");
                    } catch (Exception e) {
                        // ignore
                    }
                    tableList.add(new QueryTable(name, type, remarks, engine, charset, autoIncrementNum));
                }
            }
            // 重写 DatabaseMetaData 提高效率
            /* Map<String, QuerySchemaTableInfo> charsetMap = getTableCharset(dbName, tableList.stream().map(QueryTable::getName).collect(Collectors.toList()));
            tableList.stream().peek(p -> {
                QuerySchemaTableInfo schemaTableInfo = charsetMap.get(p.getName());
                p.setCharset(schemaTableInfo.getCharacter());
                p.setEngine(schemaTableInfo.getEngine());
                p.setAutoIncrementNum(schemaTableInfo.getAutoIncrementNum());
            }).collect(Collectors.toList()); */
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return tableList;
        } finally {
            SourceUtils.releaseConnection(connection);
            SourceUtils.closeResult(rs);
        }
        return tableList;
    }


    private Map<String, QuerySchemaTableInfo> getTableCharset(String dbName, List<String> tables) throws Exception {
        String sql = "SELECT" +
                "  CCSA.character_set_name, T.table_name, T.engine, T.auto_increment " +
                " FROM" +
                "  information_schema.`TABLES` T," +
                "  information_schema.`COLLATION_CHARACTER_SET_APPLICABILITY` CCSA" +
                " WHERE" +
                "  CCSA.collation_name = T.table_collation" +
                "  AND T.table_schema = '" + dbName + "'" +
                "  AND T.table_name in (";
        String tablesName = "'" + org.apache.commons.lang3.StringUtils.join(tables, "','") + "')";
        PaginateWithQueryColumns paginateWithQueryColumns = syncQuery4Paginate(sql + tablesName, null, null, null, tables.size(),null);
        List<Map<String, Object>> list = paginateWithQueryColumns.getResultList();
        return list.stream().collect(Collectors.toMap(p -> p.get("TABLE_NAME").toString(), p -> new QuerySchemaTableInfo(p.get("ENGINE").toString(), p.get("CHARACTER_SET_NAME").toString(), Long.valueOf(p.get("AUTO_INCREMENT").toString())), (key1, key2) -> key2));
    }

    private String getDBSchemaPattern(String schema) {
        if (dataTypeEnum == null) {
            return null;
        }
        String schemaPattern = null;
        switch (dataTypeEnum) {
            case ORACLE:
                schemaPattern = this.jdbcSourceInfo.getUsername();
                if (null != schemaPattern) {
                    schemaPattern = schemaPattern.toUpperCase();
                }
                break;
            case SQLSERVER:
                schemaPattern = "dbo";
                break;
            case CLICKHOUSE:
            case PRESTO:
                if (!StringUtils.isEmpty(schema)) {
                    schemaPattern = schema;
                }
                break;
            default:
                break;
        }
        return schemaPattern;
    }

    /**
     * 获取指定表列信息
     *
     * @param tableName
     * @return
     * @throws SourceException
     */
    public TableInfo getTableInfo(String dbName, String tableName) throws Exception {
        TableInfo tableInfo = null;
        Connection connection = null;
        try {
            connection = sourceUtils.getConnection(this.jdbcSourceInfo);
            if (null != connection) {
                DatabaseMetaData metaData = connection.getMetaData();
                List<String> primaryKeys = getPrimaryKeys(dbName, tableName, metaData);
                List<QueryExportKeys> exportedKeys = getExportedKeys(dbName, tableName, metaData);
                List<QueryColumn> columns = getColumns(dbName, tableName, metaData);
                List<QueryKey> indexs = getKeyInfo(tableName);
                tableInfo = new TableInfo(tableName, primaryKeys, exportedKeys, columns, indexs);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new SourceException(e.getMessage() + ", jdbcUrl=" + this.jdbcSourceInfo.getJdbcUrl());
        } finally {
            SourceUtils.releaseConnection(connection);
        }
        return tableInfo;
    }


    /**
     * 判断表是否存在
     *
     * @param tableName
     * @return
     * @throws SourceException
     */
    public boolean tableIsExist(String tableName) throws SourceException {
        boolean result = false;
        Connection connection = null;
        ResultSet rs = null;
        try {
            connection = sourceUtils.getConnection(this.jdbcSourceInfo);
            if (null != connection) {
                rs = connection.getMetaData().getTables(null, null, tableName, null);
                if (null != rs && rs.next()) {
                    result = true;
                } else {
                    result = false;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new SourceException("Get connection meta data error, jdbcUrl=" + this.jdbcSourceInfo.getJdbcUrl());
        } finally {
            SourceUtils.releaseConnection(connection);
            SourceUtils.closeResult(rs);
        }
        return result;
    }


    /**
     * 获取数据表主键
     *
     * @param tableName
     * @param metaData
     * @return
     * @throws ServerException
     */
    private List<String> getPrimaryKeys(String dbName, String tableName, DatabaseMetaData metaData) throws ServerException {
        ResultSet rs = null;
        List<String> primaryKeys = new ArrayList<>();
        try {
            rs = metaData.getPrimaryKeys(dbName, null, tableName);
            if (rs == null) {
                return primaryKeys;
            }
            while (rs.next()) {
                primaryKeys.add(rs.getString("COLUMN_NAME"));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            SourceUtils.closeResult(rs);
        }
        return primaryKeys;
    }

    /**
     * 获得一个表的外键信息
     * @param dbName
     * @param tableName
     * @param metaData
     */
    public List<QueryExportKeys> getExportedKeys(String dbName, String tableName, DatabaseMetaData metaData) {
        ResultSet rs = null;
        List<QueryExportKeys> exportKeys = new ArrayList<>();
        try {
            rs = metaData.getExportedKeys(null, dbName, tableName);
            while (rs.next()) {
                QueryExportKeys queryExportKeys = new QueryExportKeys();
                // String pkTableCat = rs.getString("PKTABLE_CAT");        //主键表的目录（可能为空）
                // String pkTableSchem = rs.getString("PKTABLE_SCHEM");    //主键表的架构（可能为空）
                queryExportKeys.setPkTableName(rs.getString("PKTABLE_NAME"));      //主键表名

                queryExportKeys.setPkColumnName(rs.getString("PKCOLUMN_NAME"));    //主键列名
                // String fkTableCat = rs.getString("FKTABLE_CAT");                 //外键的表的目录（可能为空）出口（可能为null）
                // String fkTableSchem = rs.getString("FKTABLE_SCHEM");             //外键表的架构（可能为空）出口（可能为空）
                queryExportKeys.setFkTableName(rs.getString("FKTABLE_NAME"));      //外键表名
                queryExportKeys.setFkColumnName(rs.getString("FKCOLUMN_NAME"));     //外键列名
                // short keySeq = rs.getShort("KEY_SEQ");                  //序列号（外键内值1表示第一列的外键，值2代表在第二列的外键）。
                /**
                 * hat happens to foreign key when primary is updated:
                 * importedNoAction - do not allow update of primary key if it has been imported
                 * importedKeyCascade - change imported key to agree with primary key update
                 * importedKeySetNull - change imported key to NULL if its primary key has been updated
                 * importedKeySetDefault - change imported key to default values if its primary key has been updated
                 * importedKeyRestrict - same as importedKeyNoAction (for ODBC 2.x compatibility)
                 */
                queryExportKeys.setUpdateRule(rs.getShort("UPDATE_RULE"));
                /**
                 * What happens to the foreign key when primary is deleted.
                 * importedKeyNoAction - do not allow delete of primary key if it has been imported
                 * importedKeyCascade - delete rows that import a deleted key
                 * importedKeySetNull - change imported key to NULL if its primary key has been deleted
                 * importedKeyRestrict - same as importedKeyNoAction (for ODBC 2.x compatibility)
                 * importedKeySetDefault - change imported key to default if its primary key has been deleted
                 */
                queryExportKeys.setDelRule(rs.getShort("DELETE_RULE"));
                queryExportKeys.setFkName(rs.getString("FK_NAME")); //外键的名称（可能为空）
                queryExportKeys.setPkName(rs.getString("PK_NAME"));//主键的名称（可能为空）
                /**
                 * can the evaluation of foreign key constraints be deferred until commit
                 * importedKeyInitiallyDeferred - see SQL92 for definition
                 * importedKeyInitiallyImmediate - see SQL92 for definition
                 * importedKeyNotDeferrable - see SQL92 for definition
                 */
                // short deferRability = rs.getShort("DEFERRABILITY");
                exportKeys.add(queryExportKeys);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        } finally {
            SourceUtils.closeResult(rs);
        }
        return exportKeys;
    }


    /**
     * 获取数据表列
     *
     * @param tableName
     * @param metaData
     * @return
     * @throws ServerException
     */
    private List<QueryColumn> getColumns(String dbName, String tableName, DatabaseMetaData metaData) throws ServerException {
        ResultSet rs = null;
        List<QueryColumn> columnList = new ArrayList<>();
        try {
            if (this.dataTypeEnum == ORACLE) {
                dbName = null;
            }
            rs = metaData.getColumns(dbName, null, tableName, "%");

            if (rs == null) {
                return columnList;
            }
            while (rs.next()) {
                QueryColumn queryColumn = new QueryColumn(rs.getString("COLUMN_NAME"), rs.getString("TYPE_NAME"));
                /**
                 *  0 (columnNoNulls) - 该列不允许为空
                 *  1 (columnNullable) - 该列允许为空
                 *  2 (columnNullableUnknown) - 不确定该列是否为空
                 */
                // int nullAble = rs.getInt("NULLABLE");  //是否允许为null
                // ISO规则用来确定某一列的是否可为空(等同于NULLABLE的值:[ 0:'YES'; 1:'NO'; 2:''; ])  返回 YES / NO
                queryColumn.setNullAble(rs.getString("IS_NULLABLE"));
                // 备注
                queryColumn.setComment(rs.getString("REMARKS"));
                //列大小
                queryColumn.setSize(rs.getString("COLUMN_SIZE"));
                // 是否自动增长 返回 YES / NO
                queryColumn.setAutoIncrement(rs.getString("IS_AUTOINCREMENT"));
                // 默认值
                queryColumn.setDefaultValue(rs.getString("COLUMN_DEF"));
                //小数位数
                queryColumn.setDecimalDigits(rs.getInt("DECIMAL_DIGITS"));
                //基数（通常是10或2）对于 DECIMAL_DIGITS 不适用的数据类型，则返回 Null。
                queryColumn.setNumPrecRadix(rs.getInt("NUM_PREC_RADIX"));
                String extra = rs.getString("EXTRA");
                queryColumn.setExtra(extra);
                Boolean onUpdate = org.apache.commons.lang3.StringUtils.isNotBlank(extra) && extra.toLowerCase().contains("on update current_timestamp");
                queryColumn.setDateOnUpdate(onUpdate);
                columnList.add(queryColumn);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            SourceUtils.closeResult(rs);
        }
        return columnList;
    }


    public List<QueryKey> getKeyInfo(String tableName) throws Exception {
        List<QueryKey> resList = new ArrayList<>();
        String sql = "SHOW CREATE TABLE `" + tableName + "`";
        // 重新创建连接, 不要复用 不然会报错 如下示例
        // The last packet successfully received from the server was 1,612,628 milliseconds ago.
        // The last packet sent successfully to the server was 1,612,636 milliseconds ago.
        this.init(this.source);
        PaginateWithQueryColumns paginateWithQueryColumns = syncQuery4Paginate(sql, null, null, null, 1,null);
        List<Map<String, Object>> list = paginateWithQueryColumns.getResultList();
        Map<String, Object> map = list.get(0);
        String text = map.get("Create Table").toString().toLowerCase();
        String temp = org.apache.commons.lang3.StringUtils.substringAfter(text, "(");
        String temp2 = org.apache.commons.lang3.StringUtils.substringBeforeLast(temp, ")");
        String [] arr = org.apache.commons.lang3.StringUtils.split(temp2, "\n");
        List<String> keyTextList = Arrays.stream(arr).filter(p -> p.contains("key") || p.contains("index")).collect(Collectors.toList());
        for(int i = 0; i < keyTextList.size(); i++) {
            QueryKey queryKey = new QueryKey();
            String item = keyTextList.get(i);
            // 获取 索引类型
            String type = getKeyType(item);
            queryKey.setIndexType(type);
            // 获取索引名称
            String indexName = getKeyName(item, type);
            queryKey.setIndexName(indexName);
            // 获取索引字段
            String fieldInfoAfter = org.apache.commons.lang3.StringUtils.substringAfter(item, "(");
            String fieldInfo = org.apache.commons.lang3.StringUtils.substringBeforeLast(fieldInfoAfter, ")");
            String[] columns = org.apache.commons.lang3.StringUtils.split(fieldInfo, ",");
            List<IndexColumn> columnsList = getKeyColumns(columns);
            queryKey.setIndexColumns(columnsList);
            resList.add(queryKey);
        }
        return resList;
    }

    private List<IndexColumn> getKeyColumns(String[] columns) {
        List<IndexColumn> columnsList = Arrays.stream(columns).map(p -> {
            // `id`
            // `name`(2) DESC
            IndexColumn indexColumn = new IndexColumn();
            String columnName = org.apache.commons.lang3.StringUtils.substringBetween(p, "`", "`");
            indexColumn.setColumn(columnName);
            String regEx="[^0-9]";
            Pattern pattern = Pattern.compile(regEx);
            Matcher m = pattern.matcher(p);
            // 前缀限制长度
            String length = m.replaceAll("").trim();
            indexColumn.setPrefixSize(length);
            String order = p.contains("desc") ? "DESC" : "ASC";
            indexColumn.setOrder(order);
            return indexColumn;
        }).collect(Collectors.toList());
        return columnsList;
    }

    private String getKeyName(String item, String type) {
        String indexName = "";
        if(type.equals("Primary")) {
            // 主键索引一定是 PRIMARY, 但是在建表语句中是省略的
            indexName = "PRIMARY";
        } else {
            String itemAfter = org.apache.commons.lang3.StringUtils.substringAfter(item, "key");
            indexName = org.apache.commons.lang3.StringUtils.substringBefore(itemAfter, "(").replaceAll("`", "").trim();
        }
        return indexName;
    }

    private String getKeyType(String item) {
        String type = "Normal";
        String keyType = org.apache.commons.lang3.StringUtils.substringBefore(item, "key");
        if(org.apache.commons.lang3.StringUtils.isNotBlank(keyType)) {
            if(keyType.trim().equals("primary") ) {
                type = "Primary";
            }
            if(keyType.trim().equals("unique")) {
                type = "Unique";
            }
            if(keyType.trim().equals("fullText")) {
                type = "FullText";
            }
            if(keyType.trim().equals("spatial")) {
                type = "Spatial";
            }
        }
        return type;
    }

    /**
     * 获得一个表的索引信息
     * getIndexInfo
     * catalog : 类别名称，因为存储在此数据库中，所以它必须匹配类别名称。该参数为 “” 则检索没有类别的描述，为 null 则表示该类别名称不应用于缩小搜索范围
     * schema : 模式名称，因为存储在此数据库中，所以它必须匹配模式名称。该参数为 “” 则检索那些没有模式的描述，为 null 则表示该模式名称不应用于缩小搜索范围
     * table : 表名称，因为存储在此数据库中，所以它必须匹配表名称
     * unique : 该参数为 true 时，仅返回惟一值的索引；该参数为 false 时，返回所有索引，不管它们是否惟一
     * approximate : 该参数为 true 时，允许结果是接近的数据值或这些数据值以外的值；该参数为 false 时，要求结果是精确结果
     */
    public void queryIndexInfo(String dbName, String tableName, DatabaseMetaData metaData) {
        ResultSet rs = null;
        try {
            rs = metaData.getIndexInfo(null, dbName, tableName, false, true);
            while (rs.next()) {
                String ascOrDesc = rs.getString("ASC_OR_DESC");         // 列排序顺序: 升序还是降序
                int cardinality = rs.getInt("CARDINALITY");             // 基数
                short ordinalPosition = rs.getShort("ORDINAL_POSITION");// 在索引列顺序号
                boolean nonUnique = rs.getBoolean("NON_UNIQUE");        // 非唯一索引(Can index values be non-unique. false when TYPE is  tableIndexStatistic   )
                String indexQualifier = rs.getString("INDEX_QUALIFIER");// 索引目录（可能为空）
                String indexName = rs.getString("INDEX_NAME");          // 索引的名称
                short indexType = rs.getShort("TYPE");          // 索引类型
                String columnName = rs.getString("COLUMN_NAME");
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        } finally {
            //
        }
    }

    /**
     * 获得一个表的索引信息
     * getIndexInfo
     * catalog : 类别名称，因为存储在此数据库中，所以它必须匹配类别名称。该参数为 “” 则检索没有类别的描述，为 null 则表示该类别名称不应用于缩小搜索范围
     * schema : 模式名称，因为存储在此数据库中，所以它必须匹配模式名称。该参数为 “” 则检索那些没有模式的描述，为 null 则表示该模式名称不应用于缩小搜索范围
     * table : 表名称，因为存储在此数据库中，所以它必须匹配表名称
     * unique : 该参数为 true 时，仅返回惟一值的索引；该参数为 false 时，返回所有索引，不管它们是否惟一
     * approximate : 该参数为 true 时，允许结果是接近的数据值或这些数据值以外的值；该参数为 false 时，要求结果是精确结果
     */
    public List<QueryIndex> getIndexInfo(String dbName, String tableName, DatabaseMetaData metaData) {
        ResultSet rs = null;
        List<QueryIndex> indexList = new ArrayList<>();
        try {
            rs = metaData.getIndexInfo(null, dbName, tableName, false, true);
            while (rs.next()) {
                QueryIndex queryIndex = new QueryIndex();
                String ascOrDesc = rs.getString("ASC_OR_DESC");         // 列排序顺序: 升序还是降序
                int cardinality = rs.getInt("CARDINALITY");             // 基数
                short ordinalPosition = rs.getShort("ORDINAL_POSITION");// 在索引列顺序号
                boolean nonUnique = rs.getBoolean("NON_UNIQUE");        // 非唯一索引(Can index values be non-unique. false when TYPE is  tableIndexStatistic   )
                String indexQualifier = rs.getString("INDEX_QUALIFIER");// 索引目录（可能为空）
                queryIndex.setIndexName(rs.getString("INDEX_NAME"));    // 索引的名称
                //queryIndex.setIndexType(rs.getShort("TYPE"));           // 索引类型
                if(nonUnique) {
                    queryIndex.setIndexType("Unique");
                } else {
                    queryIndex.setIndexType("Normal");
                }
                queryIndex.setIndexColumn(rs.getString("COLUMN_NAME"));
                indexList.add(queryIndex);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }finally {
            SourceUtils.closeResult(rs);
        }
        return indexList;
    }


    /**
     * 检查敏感操作
     *
     * @param sql
     * @throws ServerException
     */
    public static void checkSensitiveSql(String sql) throws ServerException {
        Matcher matcher = PATTERN_SENSITIVE_SQL.matcher(sql.toLowerCase());
        if (matcher.find()) {
            String group = matcher.group();
            log.warn("Sensitive SQL operations are not allowed: {}", group.toUpperCase());
            throw new ServerException("Sensitive SQL operations are not allowed: " + group.toUpperCase());
        }
    }


    public JdbcTemplate jdbcTemplate() throws SourceException {
        Connection connection = null;
        try {
            connection = sourceUtils.getConnection(this.jdbcSourceInfo);
        } catch (SourceException e) {
        }
        if (connection == null) {
            sourceUtils.releaseDataSource(this.jdbcSourceInfo);
        } else {
            SourceUtils.releaseConnection(connection);
        }
        DataSource dataSource = sourceUtils.getDataSource(this.jdbcSourceInfo);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.setFetchSize(500);
        return jdbcTemplate;
    }

    public boolean testConnection() throws SourceException {
        Connection connection = null;
        try {
            connection = sourceUtils.getConnection(jdbcSourceInfo);
            if (null != connection) {
                return true;
            } else {
                return false;
            }
        } catch (SourceException sourceException) {
            throw sourceException;
        } finally {
            SourceUtils.releaseConnection(connection);
            sourceUtils.releaseDataSource(jdbcSourceInfo);
        }
    }

    public void executeBatch(String sql, Set<QueryColumn> headers, List<Map<String, Object>> datas) throws ServerException {

        if (StringUtils.isEmpty(sql)) {
            log.info("execute batch sql is EMPTY");
            throw new ServerException("execute batch sql is EMPTY");
        }

        if (CollectionUtils.isEmpty(datas)) {
            log.info("execute batch data is EMPTY");
            throw new ServerException("execute batch data is EMPTY");
        }

        Connection connection = null;
        PreparedStatement pstmt = null;
        try {
            connection = sourceUtils.getConnection(this.jdbcSourceInfo);
            if (null != connection) {
                connection.setAutoCommit(false);
                pstmt = connection.prepareStatement(sql);
                //每1000条commit一次
                int n = 10000;

                for (Map<String, Object> map : datas) {
                    int i = 1;
                    for (QueryColumn queryColumn : headers) {
                        Object obj = map.get(queryColumn.getName());
                        switch (SqlColumnEnum.toJavaType(queryColumn.getType())) {
                            case "Short":
                                pstmt.setShort(i, null == obj || String.valueOf(obj).equals(EMPTY) ? (short) 0 : Short.parseShort(String.valueOf(obj).trim()));
                                break;
                            case "Integer":
                                pstmt.setInt(i, null == obj || String.valueOf(obj).equals(EMPTY) ? 0 : Integer.parseInt(String.valueOf(obj).trim()));
                                break;
                            case "Long":
                                pstmt.setLong(i, null == obj || String.valueOf(obj).equals(EMPTY) ? 0L : Long.parseLong(String.valueOf(obj).trim()));
                                break;
                            case "BigDecimal":
                                pstmt.setBigDecimal(i, null == obj || String.valueOf(obj).equals(EMPTY) ? null : (BigDecimal) obj);
                                break;
                            case "Float":
                                pstmt.setFloat(i, null == obj || String.valueOf(obj).equals(EMPTY) ? 0.0F : Float.parseFloat(String.valueOf(obj).trim()));
                                break;
                            case "Double":
                                pstmt.setDouble(i, null == obj || String.valueOf(obj).equals(EMPTY) ? 0.0D : Double.parseDouble(String.valueOf(obj).trim()));
                                break;
                            case "String":
                                pstmt.setString(i, (String) obj);
                                break;
                            case "Boolean":
                                pstmt.setBoolean(i, null != obj && Boolean.parseBoolean(String.valueOf(obj).trim()));
                                break;
                            case "Bytes":
                                pstmt.setBytes(i, (byte[]) obj);
                                break;
                            case "Date":
                                if (obj == null) {
                                    pstmt.setDate(i, null);
                                } else {
                                    java.util.Date date = (java.util.Date) obj;
                                    pstmt.setDate(i, DateUtils.toSqlDate(date));
                                }
                                break;
                            case "DateTime":
                                if (obj == null) {
                                    pstmt.setTimestamp(i, null);
                                } else {
                                    if (obj instanceof LocalDateTime) {
                                        pstmt.setTimestamp(i, Timestamp.valueOf((LocalDateTime) obj));
                                    } else {
                                        DateTime dateTime = (DateTime) obj;
                                        pstmt.setTimestamp(i, DateUtils.toTimestamp(dateTime));
                                    }
                                }
                                break;
                            case "Timestamp":
                                if (obj == null) {
                                    pstmt.setTimestamp(i, null);
                                } else {
                                    if (obj instanceof LocalDateTime) {
                                        pstmt.setTimestamp(i, Timestamp.valueOf((LocalDateTime) obj));
                                    } else {
                                        pstmt.setTimestamp(i, (Timestamp) obj);
                                    }
                                }
                                break;
                            case "Blob":
                                pstmt.setBlob(i, null == obj ? null : (Blob) obj);
                                break;
                            case "Clob":
                                pstmt.setClob(i, null == obj ? null : (Clob) obj);
                                break;
                            default:
                                pstmt.setObject(i, obj);
                        }
                        i++;
                    }

                    pstmt.addBatch();
                    if (i % n == 0) {
                        try {
                            pstmt.executeBatch();
                            connection.commit();
                        } catch (BatchUpdateException e) {
                        }
                    }
                }

                pstmt.executeBatch();
                connection.commit();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (null != connection) {
                try {
                    connection.rollback();
                } catch (SQLException se) {
                    log.error(se.getMessage(), se);
                }
            }
            throw new ServerException(e.getMessage(), e);
        } finally {
            if (null != pstmt) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    log.error(e.getMessage(), e);
                    throw new ServerException(e.getMessage(), e);
                }
            }
            SourceUtils.releaseConnection(connection);
        }
    }

    public static String getKeywordPrefix(String jdbcUrl, String dbVersion) {
        String keywordPrefix = "";
        CustomDataSource customDataSource = CustomDataSourceUtils.getInstance(jdbcUrl, dbVersion);
        if (null != customDataSource) {
            keywordPrefix = customDataSource.getKeyword_prefix();
        } else {
            DataTypeEnum dataTypeEnum = DataTypeEnum.urlOf(jdbcUrl);
            if (null != dataTypeEnum) {
                keywordPrefix = dataTypeEnum.getKeywordPrefix();
            }
        }
        return StringUtils.isEmpty(keywordPrefix) ? EMPTY : keywordPrefix;
    }

    public static String getKeywordSuffix(String jdbcUrl, String dbVersion) {
        String keywordSuffix = "";
        CustomDataSource customDataSource = CustomDataSourceUtils.getInstance(jdbcUrl, dbVersion);
        if (null != customDataSource) {
            keywordSuffix = customDataSource.getKeyword_suffix();
        } else {
            DataTypeEnum dataTypeEnum = DataTypeEnum.urlOf(jdbcUrl);
            if (null != dataTypeEnum) {
                keywordSuffix = dataTypeEnum.getKeywordSuffix();
            }
        }
        return StringUtils.isEmpty(keywordSuffix) ? EMPTY : keywordSuffix;
    }

    public static String getAliasPrefix(String jdbcUrl, String dbVersion) {
        String aliasPrefix = "";
        CustomDataSource customDataSource = CustomDataSourceUtils.getInstance(jdbcUrl, dbVersion);
        if (null != customDataSource) {
            aliasPrefix = customDataSource.getAlias_prefix();
        } else {
            DataTypeEnum dataTypeEnum = DataTypeEnum.urlOf(jdbcUrl);
            if (null != dataTypeEnum) {
                aliasPrefix = dataTypeEnum.getAliasPrefix();
            }
        }
        return StringUtils.isEmpty(aliasPrefix) ? EMPTY : aliasPrefix;
    }

    public static String getAliasSuffix(String jdbcUrl, String dbVersion) {
        String aliasSuffix = "";
        CustomDataSource customDataSource = CustomDataSourceUtils.getInstance(jdbcUrl, dbVersion);
        if (null != customDataSource) {
            aliasSuffix = customDataSource.getAlias_suffix();
        } else {
            DataTypeEnum dataTypeEnum = DataTypeEnum.urlOf(jdbcUrl);
            if (null != dataTypeEnum) {
                aliasSuffix = dataTypeEnum.getAliasSuffix();
            }
        }
        return StringUtils.isEmpty(aliasSuffix) ? EMPTY : aliasSuffix;
    }


    /**
     * 过滤sql中的注释
     *
     * @param sql
     * @return
     */
    public static String filterAnnotate(String sql) {
        sql = PATTERN_SQL_ANNOTATE.matcher(sql).replaceAll("$1");
        sql = sql.replaceAll(NEW_LINE_CHAR, SPACE).replaceAll("(;+\\s*)+", SEMICOLON);
        return sql;
    }

    public static String formatSqlType(String type) throws ServerException {
        if (!StringUtils.isEmpty(type.trim())) {
            type = type.trim().toUpperCase();
            Matcher matcher = PATTERN_DB_COLUMN_TYPE.matcher(type);
            if (!matcher.find()) {
                return SqlTypeEnum.getType(type);
            } else {
                return type;
            }
        }
        return null;
    }

    private static FromItemVisitor getFromItemTableName(Set<String> set) {
        return new FromItemVisitor() {
            @Override
            public void visit(Table tableName) {
                set.add(tableName.getName() + DOT);
            }

            @Override
            public void visit(SubSelect subSelect) {
            }

            @Override
            public void visit(SubJoin subjoin) {
            }

            @Override
            public void visit(LateralSubSelect lateralSubSelect) {
            }

            @Override
            public void visit(ValuesList valuesList) {
            }

            @Override
            public void visit(TableFunction tableFunction) {
            }

            @Override
            public void visit(ParenthesisFromItem aThis) {
            }
        };
    }


    public SqlUtils() {

    }

    public SqlUtils(JdbcSourceInfo jdbcSourceInfo) {
        this.jdbcSourceInfo = jdbcSourceInfo;
        this.dataTypeEnum = DataTypeEnum.urlOf(jdbcSourceInfo.getJdbcUrl());
    }

    public static final class SqlUtilsBuilder {
        private JdbcDataSource jdbcDataSource;
        private int resultLimit;
        private boolean isQueryLogEnable;
        private String jdbcUrl;
        private String username;
        private String password;
        private List<Dict> properties;
        private String dbVersion;
        private boolean isExt;

        private SqlUtilsBuilder() {

        }

        public static SqlUtilsBuilder getBuilder() {
            return new SqlUtilsBuilder();
        }

        SqlUtilsBuilder withJdbcDataSource(JdbcDataSource jdbcDataSource) {
            this.jdbcDataSource = jdbcDataSource;
            return this;
        }

        SqlUtilsBuilder withResultLimit(int resultLimit) {
            this.resultLimit = resultLimit;
            return this;
        }

        SqlUtilsBuilder withIsQueryLogEnable(boolean isQueryLogEnable) {
            this.isQueryLogEnable = isQueryLogEnable;
            return this;
        }

        SqlUtilsBuilder withJdbcUrl(String jdbcUrl) {
            this.jdbcUrl = jdbcUrl;
            return this;
        }

        SqlUtilsBuilder withUsername(String username) {
            this.username = username;
            return this;
        }

        SqlUtilsBuilder withPassword(String password) {
            this.password = password;
            return this;
        }

        SqlUtilsBuilder withProperties(List<Dict> properties) {
            this.properties = properties;
            return this;
        }

        SqlUtilsBuilder withDbVersion(String dbVersion) {
            this.dbVersion = dbVersion;
            return this;
        }

        SqlUtilsBuilder withIsExt(boolean isExt) {
            this.isExt = isExt;
            return this;
        }

        public SqlUtils build() throws ServerException {

            String datasource = SourceUtils.isSupportedDatasource(jdbcUrl);
            SourceUtils.checkDriver(datasource, jdbcUrl, dbVersion, isExt);

            JdbcSourceInfo jdbcSourceInfo = JdbcSourceInfo
                    .JdbcSourceInfoBuilder
                    .aJdbcSourceInfo()
                    .withJdbcUrl(this.jdbcUrl)
                    .withUsername(this.username)
                    .withPassword(this.password)
                    .withDatabase(datasource)
                    .withDbVersion(this.dbVersion)
                    .withProperties(this.properties)
                    .withExt(this.isExt)
                    .build();

            SqlUtils sqlUtils = new SqlUtils(jdbcSourceInfo);
            sqlUtils.jdbcDataSource = this.jdbcDataSource;
            sqlUtils.resultLimit = this.resultLimit;
            sqlUtils.isQueryLogEnable = this.isQueryLogEnable;
            sqlUtils.sourceUtils = new SourceUtils(this.jdbcDataSource);
            return sqlUtils;
        }
    }

    public String getJdbcUrl() {
        if (this.jdbcSourceInfo == null) {
            return null;
        }
        return this.jdbcSourceInfo.getJdbcUrl();
    }

    public static String formatSql(String sql) {
        return SQLUtils.formatMySql(sql);
    }
}
