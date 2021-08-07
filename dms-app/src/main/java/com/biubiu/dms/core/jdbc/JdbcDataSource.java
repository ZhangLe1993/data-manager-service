package com.biubiu.dms.core.jdbc;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.ElasticSearchDruidDataSourceFactory;
import com.alibaba.druid.util.StringUtils;

import com.biubiu.dms.core.consts.Consts;
import com.biubiu.dms.core.enums.DataTypeEnum;
import com.biubiu.dms.core.exception.SourceException;
import com.biubiu.dms.core.model.JdbcSourceInfo;
import com.biubiu.dms.core.utils.CollectionUtils;
import com.biubiu.dms.core.utils.SourceUtils;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.alibaba.druid.pool.DruidDataSourceFactory.*;


@Component
public class JdbcDataSource {

    private static final Logger log = LoggerFactory.getLogger(JdbcDataSource.class);

    @Component
    private class ESDataSource extends JdbcDataSource {

        public DruidDataSource getDataSource(JdbcSourceInfo jdbcSourceInfo) throws SourceException {

            String jdbcUrl = jdbcSourceInfo.getJdbcUrl();
            String username = jdbcSourceInfo.getUsername();
            String password = jdbcSourceInfo.getPassword();

            String key = getDataSourceKey(jdbcSourceInfo);

            DruidDataSource druidDataSource = dataSourceMap.get(key);
            if (druidDataSource != null && !druidDataSource.isClosed()) {
                return druidDataSource;
            }

            Lock lock = getDataSourceLock(key);

            try {
                if (!lock.tryLock(5L, TimeUnit.SECONDS)) {
                    throw new SourceException("Unable to get driver instance for jdbcUrl: " + jdbcUrl);
                }
            }
            catch (InterruptedException e) {
                throw new SourceException("Unable to get driver instance for jdbcUrl: " + jdbcUrl);
            }

            druidDataSource = dataSourceMap.get(key);
            if (druidDataSource != null && !druidDataSource.isClosed()) {
                return druidDataSource;
            }

            Properties properties = new Properties();
            properties.setProperty(PROP_URL, jdbcUrl.trim());
            if (!StringUtils.isEmpty(username)) {
                properties.setProperty(PROP_USERNAME, username);
            }

            if (!StringUtils.isEmpty(password)) {
                properties.setProperty(PROP_PASSWORD, password);
            }

            properties.setProperty(PROP_MAXACTIVE, String.valueOf(maxActive));
            properties.setProperty(PROP_INITIALSIZE, String.valueOf(initialSize));
            properties.setProperty(PROP_MINIDLE, String.valueOf(minIdle));
            properties.setProperty(PROP_MAXWAIT, String.valueOf(maxWait));
            properties.setProperty(PROP_TIMEBETWEENEVICTIONRUNSMILLIS, String.valueOf(timeBetweenEvictionRunsMillis));
            properties.setProperty(PROP_MINEVICTABLEIDLETIMEMILLIS, String.valueOf(minEvictableIdleTimeMillis));
            properties.setProperty(PROP_TESTWHILEIDLE, String.valueOf(false));
            properties.setProperty(PROP_TESTONBORROW, String.valueOf(testOnBorrow));
            properties.setProperty(PROP_TESTONRETURN, String.valueOf(testOnReturn));
            properties.put(PROP_CONNECTIONPROPERTIES, "client.transport.ignore_cluster_name=true");

            if (!CollectionUtils.isEmpty(jdbcSourceInfo.getProperties())) {
                jdbcSourceInfo.getProperties().forEach(dict -> properties.setProperty(dict.getKey(), dict.getValue()));
            }

            try {
                druidDataSource = (DruidDataSource)ElasticSearchDruidDataSourceFactory.createDataSource(properties);
                dataSourceMap.put(key, druidDataSource);
            } catch (Exception e) {
                log.error("Exception during pool initialization", e);
                throw new SourceException(e.getMessage());
            }finally {
                lock.unlock();
            }

            return druidDataSource;
        }
    }

    @Resource
    private ESDataSource esDataSource;

    @Value("${source.datasource.type}")
    protected String type;

    @Value("${source.max-active:10}")
    @Getter
    protected int maxActive;

    @Value("${source.initial-size:1}")
    @Getter
    protected int initialSize;

    @Value("${source.min-idle:3}")
    @Getter
    protected int minIdle;

    @Value("${source.max-wait:30000}")
    @Getter
    protected long maxWait;

    @Value("${source.datasource.time-between-eviction-runs-millis}")
    @Getter
    protected long timeBetweenEvictionRunsMillis;

    @Value("${source.datasource.min-evictable-idle-time-millis}")
    @Getter
    protected long minEvictableIdleTimeMillis;

    @Value("${source.datasource.test-while-idle}")
    @Getter
    protected boolean testWhileIdle;

    @Value("${source.datasource.test-on-borrow}")
    @Getter
    protected boolean testOnBorrow;

    @Value("${source.datasource.test-on-return}")
    @Getter
    protected boolean testOnReturn;

    @Value("${source.break-after-acquire-failure:true}")
    @Getter
    protected boolean breakAfterAcquireFailure;

    @Value("${source.connection-error-retry-attempts:0}")
    @Getter
    protected int connectionErrorRetryAttempts;

    @Value("${source.query-timeout:600000}")
    @Getter
    protected int queryTimeout;

    private static volatile Map<String, DruidDataSource> dataSourceMap = new ConcurrentHashMap<>();
    private static volatile Map<String, Lock> dataSourceLockMap = new ConcurrentHashMap<>();
    private static final Object lockLock = new Object();

    private Lock getDataSourceLock(String key) {
        if (dataSourceLockMap.containsKey(key)) {
            return dataSourceLockMap.get(key);
        }

        synchronized (lockLock) {
            if (dataSourceLockMap.containsKey(key)) {
                return dataSourceLockMap.get(key);
            }
            Lock lock = new ReentrantLock();
            dataSourceLockMap.put(key, lock);
            return lock;
        }
    }

    /**
     * only for test
     * @param jdbcSourceInfo
     * @return
     */
    public boolean isDataSourceExist(JdbcSourceInfo jdbcSourceInfo) {
        return dataSourceMap.containsKey(getDataSourceKey(jdbcSourceInfo));
    }

    public void removeDatasource(JdbcSourceInfo jdbcSourceInfo) {

        String key = getDataSourceKey(jdbcSourceInfo);

        Lock lock = getDataSourceLock(key);

        if (!lock.tryLock()) {
            return;
        }

        try {
            DruidDataSource druidDataSource = dataSourceMap.remove(key);
            if (druidDataSource != null) {
                druidDataSource.close();
            }

            dataSourceLockMap.remove(key);
        }finally {
            lock.unlock();
        }
    }

    public DruidDataSource getDataSource(JdbcSourceInfo jdbcSourceInfo) throws SourceException {
        boolean ext = jdbcSourceInfo.isExt();
        if (jdbcSourceInfo.getJdbcUrl().toLowerCase().contains(DataTypeEnum.ELASTICSEARCH.getDesc().toLowerCase()) && !ext) {
            return esDataSource.getDataSource(jdbcSourceInfo);
        }

        String jdbcUrl = jdbcSourceInfo.getJdbcUrl();
        String username = jdbcSourceInfo.getUsername();
        String password = jdbcSourceInfo.getPassword();
        String dbVersion = jdbcSourceInfo.getDbVersion();

        String key = getDataSourceKey(jdbcSourceInfo);

        DruidDataSource druidDataSource = dataSourceMap.get(key);
        if (druidDataSource != null && !druidDataSource.isClosed()) {
            return druidDataSource;
        }

        Lock lock = getDataSourceLock(key);

        try {
            if (!lock.tryLock(5L, TimeUnit.SECONDS)) {
                throw new SourceException("Unable to get driver instance for jdbcUrl: " + jdbcUrl);
            }
        }
        catch (InterruptedException e) {
            throw new SourceException("Unable to get driver instance for jdbcUrl: " + jdbcUrl);
        }

        druidDataSource = dataSourceMap.get(key);
        if (druidDataSource != null && !druidDataSource.isClosed()) {
            return druidDataSource;
        }
        druidDataSource = new DruidDataSource();
        try {
            if (StringUtils.isEmpty(dbVersion) || !ext || Consts.JDBC_DATASOURCE_DEFAULT_VERSION.equals(dbVersion)) {
                String className = SourceUtils.getDriverClassName(jdbcUrl, null);
                try {
                    Class.forName(className);
                } catch (ClassNotFoundException e) {
                    throw new SourceException("Unable to get driver instance for jdbcUrl: " + jdbcUrl);
                }
                druidDataSource.setDriverClassName(className);
            } else {
                // ignore
                // 暂时不考虑带版本好的数据库 链接
                // druidDataSource.setDriverClassName(CustomDataSourceUtils.getInstance(jdbcUrl, dbVersion).getDriver());
                // String path = System.getenv("dms_home") + File.separator  + String.format(Consts.PATH_EXT_FORMATER, jdbcSourceInfo.getDatabase(), dbVersion);
                // druidDataSource.setDriverClassLoader(ExtendedJdbcClassLoader.getExtJdbcClassLoader(path));
            }
            druidDataSource.setUrl(jdbcUrl);
            druidDataSource.setUsername(username);

            if (!jdbcUrl.toLowerCase().contains(DataTypeEnum.PRESTO.getFeature())) {
                druidDataSource.setPassword(password);
            }

            druidDataSource.setInitialSize(initialSize);
            druidDataSource.setMinIdle(minIdle);
            druidDataSource.setMaxActive(maxActive);
            druidDataSource.setMaxWait(maxWait);
            druidDataSource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
            druidDataSource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
            druidDataSource.setTestWhileIdle(false);
            druidDataSource.setTestOnBorrow(testOnBorrow);
            druidDataSource.setTestOnReturn(testOnReturn);
            druidDataSource.setConnectionErrorRetryAttempts(connectionErrorRetryAttempts);
            druidDataSource.setBreakAfterAcquireFailure(breakAfterAcquireFailure);

            String driverName=druidDataSource.getDriverClassName();
            if(driverName.contains("sqlserver")) {
                druidDataSource.setValidationQuery("select 1");
            }

            if (!CollectionUtils.isEmpty(jdbcSourceInfo.getProperties())) {
                Properties properties = new Properties();
                jdbcSourceInfo.getProperties().forEach(dict -> properties.setProperty(dict.getKey(), dict.getValue()));
                druidDataSource.setConnectProperties(properties);
            }

            try {
                druidDataSource.init();
            } catch (Exception e) {
                log.error("Exception during pool initialization", e);
                throw new SourceException(e.getMessage());
            }

            dataSourceMap.put(key, druidDataSource);

        }finally {
            lock.unlock();
        }

        return druidDataSource;
    }

    private String getDataSourceKey (JdbcSourceInfo jdbcSourceInfo) {
        return SourceUtils.getKey(jdbcSourceInfo.getJdbcUrl(),
                jdbcSourceInfo.getUsername(),
                jdbcSourceInfo.getPassword(),
                jdbcSourceInfo.getDbVersion(),
                jdbcSourceInfo.isExt());
    }

}
