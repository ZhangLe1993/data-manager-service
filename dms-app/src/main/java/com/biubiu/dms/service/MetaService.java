package com.biubiu.dms.service;

import com.biubiu.dms.core.exception.ServerException;
import com.biubiu.dms.core.exception.SourceException;
import com.biubiu.dms.core.model.QueryTable;
import com.biubiu.dms.core.model.TableInfo;
import com.biubiu.dms.core.utils.SqlUtils;
import com.biubiu.dms.dao.ConnectionDao;
import com.biubiu.dms.dto.SourceConfig;
import com.biubiu.dms.model.DBTables;
import com.biubiu.dms.pojo.Connection;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MetaService {

    @Autowired
    private SqlUtils sqlUtils;

    @Resource
    private ConnectionDao connectionDao;


    public boolean testConnection(SourceConfig config) {
        return sqlUtils.init(
                config.getUrl(),
                config.getUsername(),
                config.getPassword(),
                config.getVersion(),
                config.getProperties(),
                config.isExt()
        ).testConnection();
    }

    private void checkIsExist(String name, Long id) {
        if (isExist(name, id)) {
            alertNameTaken(name);
        }
    }

    public boolean isExist(String name, Long id) {
        Long sourceId = connectionDao.getByName(name);
        if (null != id && null != sourceId) {
            return !id.equals(sourceId);
        }
        return null != sourceId && sourceId > 0L;
    }

    protected void alertNameTaken(String name) throws ServerException {
        log.warn("the source name ({}) is already taken", name);
        throw new ServerException("the " + name + " name is already taken");
    }

    public List<String> getSchemas(Long id) throws NotFoundException {
        List<DBTables> list = new ArrayList<>();
        Connection source = getSource(id);
        List<String> expect = Arrays.asList("information_schema", "mysql", "sys", "performance_schema");
        return sqlUtils.init(source).getDatabases().stream().filter(p -> !expect.contains(p)).collect(Collectors.toList());
    }


    public List<DBTables> getTree(Long id) throws NotFoundException {
        List<DBTables> list = new ArrayList<>();
        Connection source = getSource(id);
        List<String> schemas = sqlUtils.init(source).getDatabases();
        for(String schema : schemas) {
            DBTables dbTable = new DBTables(schema);
            List<QueryTable> tableList = null;
            try {
                tableList = sqlUtils.init(source).getTableList(schema);
            } catch (SourceException e) {
                throw new ServerException(e.getMessage());
            }
            if (null != tableList) {
                dbTable.setTables(tableList);
            }
            list.add(dbTable);
        }
        return list;
    }

    /**
     * 获取Source的data tables
     *
     * @param id
     * @param dbName
     * @return
     */
    public DBTables getSourceTables(Long id, String dbName) throws NotFoundException {
        DBTables dbTable = new DBTables(dbName);
        Connection source = getSource(id);
        List<QueryTable> tableList = null;
        try {
            tableList = sqlUtils.init(source).getTableList(dbName);
        } catch (SourceException e) {
            throw new ServerException(e.getMessage());
        }
        if (null != tableList) {
            dbTable.setTables(tableList);
        }
        return dbTable;
    }

    private Connection getSource(Long id) throws NotFoundException {
        Connection source = connectionDao.findById(id);
        if (null == source) {
            log.warn("source (:{}) is not found", id);
            throw new NotFoundException("this source is not found");
        }
        return source;
    }

    public TableInfo getTableInfo(Long id, String dbName, String tableName) throws NotFoundException {
        Connection source = getSource(id);
        TableInfo tableInfo = null;
        try {
            tableInfo = sqlUtils.init(source).getTableInfo(dbName, tableName);
        } catch (SourceException e) {
            e.printStackTrace();
            throw new ServerException(e.getMessage());
        }
        return tableInfo;
    }

}
