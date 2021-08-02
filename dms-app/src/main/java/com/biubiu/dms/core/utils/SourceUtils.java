package com.biubiu.dms.core.utils;


import com.biubiu.dms.core.consts.Consts;
import com.biubiu.dms.core.enums.DataTypeEnum;
import com.biubiu.dms.core.exception.ServerException;
import com.biubiu.dms.core.exception.SourceException;
import com.biubiu.dms.core.jdbc.ExtendedJdbcClassLoader;
import com.biubiu.dms.core.jdbc.JdbcDataSource;
import com.biubiu.dms.core.model.CustomDataSource;
import com.biubiu.dms.core.model.JdbcSourceInfo;
import com.biubiu.dms.core.runner.LoadSupportDataSourceRunner;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;

public class SourceUtils {

    private static final Logger log = LoggerFactory.getLogger(SourceUtils.class);
    private JdbcDataSource jdbcDataSource;

    public SourceUtils(JdbcDataSource jdbcDataSource) {
        this.jdbcDataSource = jdbcDataSource;
    }

    /**
     * 获取数据源
     *
     * @param jdbcSourceInfo
     * @return
     * @throws SourceException
     */
    DataSource getDataSource(JdbcSourceInfo jdbcSourceInfo) throws SourceException {
        return jdbcDataSource.getDataSource(jdbcSourceInfo);
    }

    Connection getConnection(JdbcSourceInfo jdbcSourceInfo) throws SourceException {
        DataSource dataSource = getDataSource(jdbcSourceInfo);
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
        } catch (Exception e) {

        }

        try {
            if (null == connection) {
                log.info("connection is closed, retry get connection!");
                releaseDataSource(jdbcSourceInfo);
                dataSource = getDataSource(jdbcSourceInfo);
                connection = dataSource.getConnection();
            }
        } catch (Exception e) {
            log.error("create connection error, jdbcUrl: {}", jdbcSourceInfo.getJdbcUrl());
            throw new SourceException("create connection error, jdbcUrl: " + jdbcSourceInfo.getJdbcUrl());
        }

        try {
            if (!connection.isValid(5)) {
                log.info("connection is invalid, retry get connection!");
                releaseDataSource(jdbcSourceInfo);
                connection = null;
            }
        } catch (Exception e) {

        }

        if (null == connection) {
            try {
                dataSource = getDataSource(jdbcSourceInfo);
                connection = dataSource.getConnection();
            } catch (SQLException e) {
                log.error("create connection error, jdbcUrl: {}", jdbcSourceInfo.getJdbcUrl());
                throw new SourceException("create connection error, jdbcUrl: " + jdbcSourceInfo.getJdbcUrl());
            }
        }

        return connection;
    }

    public static void releaseConnection(Connection connection) {
        if (null != connection) {
            try {
                connection.close();
                connection = null;
            } catch (Exception e) {
                e.printStackTrace();
                log.error("connection close error", e.getMessage());
            }
        }
    }

    public static void closeResult(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
                rs = null;
            } catch (Exception e) {
                e.printStackTrace();
                log.error("resultSet close error", e.getMessage());
            }
        }
    }

    public static boolean checkDriver(String dataSourceName, String jdbcUrl, String version, boolean isExt) {

        if (StringUtils.isEmpty(dataSourceName) || !LoadSupportDataSourceRunner.getSupportDatasourceMap().containsKey(dataSourceName)) {
            throw new SourceException("Not supported data type: jdbcUrl=" + jdbcUrl);
        }

        if (isExt && !StringUtils.isEmpty(version) && !Consts.JDBC_DATASOURCE_DEFAULT_VERSION.equals(version)) {
            String path = System.getenv("DAVINCI3_HOME") + File.separator + String.format(Consts.PATH_EXT_FORMATER, dataSourceName, version);
            ExtendedJdbcClassLoader extendedJdbcClassLoader = ExtendedJdbcClassLoader.getExtJdbcClassLoader(path);
            CustomDataSource dataSource = CustomDataSourceUtils.getInstance(jdbcUrl, version);
            try {
                assert extendedJdbcClassLoader != null;
                Class<?> aClass = extendedJdbcClassLoader.loadClass(dataSource.getDriver());
                if (null == aClass) {
                    throw new SourceException("Unable to get driver instance for jdbcUrl: " + jdbcUrl);
                }
            } catch (NullPointerException en) {
                throw new ServerException("JDBC driver is not found: " + dataSourceName + ":" + version);
            } catch (ClassNotFoundException ex) {
                throw new SourceException("Unable to get driver instance for jdbcUrl: " + jdbcUrl);
            }
        } else {
            if (DataTypeEnum.ELASTICSEARCH.getDesc().equals(dataSourceName) && !isExt) {
                return true;
            } else {
                try {
                    String className = getDriverClassName(jdbcUrl, null);
                    Class.forName(className);
                } catch (Exception e) {
                    throw new SourceException("Unable to get driver instance for jdbcUrl: " + jdbcUrl, e);
                }
            }
        }
        return true;
    }

    public static String isSupportedDatasource(String jdbcUrl) {
        String dataSourceName = getDataSourceName(jdbcUrl);
        if (StringUtils.isEmpty(dataSourceName)) {
            throw new SourceException("Not supported data type: jdbcUrl=" + jdbcUrl);
        }
        if (!LoadSupportDataSourceRunner.getSupportDatasourceMap().containsKey(dataSourceName)) {
            throw new SourceException("Not supported data type: jdbcUrl=" + jdbcUrl);
        }

        String urlPrefix = String.format(Consts.JDBC_PREFIX_FORMATER, dataSourceName);
        String checkUrl = jdbcUrl.replaceFirst(Consts.DOUBLE_SLASH, Consts.EMPTY).replaceFirst(Consts.AT_SYMBOL, Consts.EMPTY);
        if (urlPrefix.equals(checkUrl)) {
            throw new SourceException("Communications link failure");
        }

        return dataSourceName;
    }

    public static String getDataSourceName(String jdbcUrl) {
        String dataSourceName = null;
        jdbcUrl = jdbcUrl.replaceAll(Consts.NEW_LINE_CHAR, Consts.EMPTY).replaceAll(Consts.SPACE, Consts.EMPTY).trim().toLowerCase();
        Matcher matcher = Consts.PATTERN_JDBC_TYPE.matcher(jdbcUrl);
        if (matcher.find()) {
            dataSourceName = matcher.group().split(Consts.COLON)[1];
        }
        return dataSourceName;
    }

    public static String getDriverClassName(String jdbcUrl, String version) {
        String className = null;
        try {
            className = DriverManager.getDriver(jdbcUrl.trim()).getClass().getName();
        } catch (SQLException e) {

        }
        if (!StringUtils.isEmpty(className) && !className.contains("com.sun.proxy") && !className.contains("net.sf.cglib.proxy")) {
            return className;
        }
        DataTypeEnum dataTypeEnum = DataTypeEnum.urlOf(jdbcUrl);
        CustomDataSource customDataSource = null;
        if (null == dataTypeEnum) {
            try {
                customDataSource = CustomDataSourceUtils.getInstance(jdbcUrl, version);
            }
            catch (Exception e) {
                throw new SourceException(e.getMessage());
            }
        }

        if (null == dataTypeEnum && null == customDataSource) {
            throw new SourceException("Not supported data type: jdbcUrl=" + jdbcUrl);
        }
        return className = null != dataTypeEnum && !StringUtils.isEmpty(dataTypeEnum.getDriver())
                ? dataTypeEnum.getDriver()
                : customDataSource.getDriver().trim();
    }


    /**
     * 释放失效数据源
     *
     * @param jdbcSourceInfo
     * @return
     */
    public void releaseDataSource(JdbcSourceInfo jdbcSourceInfo) {
        jdbcDataSource.removeDatasource(jdbcSourceInfo);
    }

    public static String getKey(String jdbcUrl, String username, String password, String version, boolean isExt) {

        StringBuilder sb = new StringBuilder();

        if (!StringUtils.isEmpty(username)) {
            sb.append(username);
        }

        if (!StringUtils.isEmpty(password)) {
            sb.append(Consts.COLON).append(password);
        }

        sb.append(Consts.AT_SYMBOL).append(jdbcUrl.trim());

        if (isExt && !StringUtils.isEmpty(version)) {
            sb.append(Consts.COLON).append(version);
        }

        return MD5Util.getMD5(sb.toString(), true, 64);
    }

    public static String getKey(String url, String username, String password) {

        StringBuilder sb = new StringBuilder();

        if (!StringUtils.isEmpty(username)) {
            sb.append(username);
        }

        if (!StringUtils.isEmpty(password)) {
            sb.append(Consts.COLON).append(password);
        }

        sb.append(Consts.AT_SYMBOL).append(url.trim());

        return MD5Util.getMD5(sb.toString(), true, 64);
    }
}
