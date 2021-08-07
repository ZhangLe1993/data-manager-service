package com.biubiu.dms.service;

import com.alibaba.druid.util.StringUtils;
import com.biubiu.dms.core.exception.NotFoundException;
import com.biubiu.dms.core.exception.ServerException;
import com.biubiu.dms.core.model.PaginateWithQueryColumns;
import com.biubiu.dms.core.model.SqlEntity;
import com.biubiu.dms.core.utils.CollectionUtils;
import com.biubiu.dms.core.utils.SqlParseUtils;
import com.biubiu.dms.core.utils.SqlUtils;
import com.biubiu.dms.dto.ViewExecuteSql;
import com.biubiu.dms.pojo.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.biubiu.dms.core.consts.Consts.COMMA;


@Service
public class ExecService {

    @Autowired
    private SqlParseUtils sqlParseUtils;

    @Autowired
    private SqlUtils sqlUtils;

    @Value("${sql_template_delimiter:$}")
    private String sqlTempDelimiter;

    @Autowired
    private ConnectionService connectionService;

    public String executeSensitiveSql(ViewExecuteSql executeSql) {
        try {
            Connection source = connectionService.getSource(executeSql.getSourceId());
            SqlEntity sqlEntity = sqlParseUtils.parseSql(executeSql.getSql(),  sqlTempDelimiter);
            if (null == sqlUtils || null == sqlEntity || StringUtils.isEmpty(sqlEntity.getSql())) {
                return "参数错误";
            }
            if (!CollectionUtils.isEmpty(sqlEntity.getQuaryParams())) {
                sqlEntity.getQuaryParams().forEach((k, v) -> {
                    if (v instanceof List && ((List) v).size() > 0) {
                        v = ((List) v).stream().collect(Collectors.joining(COMMA)).toString();
                    }
                    sqlEntity.getQuaryParams().put(k, v);
                });
            }
            SqlUtils sqlUtils = this.sqlUtils.init(source);
            List<String> executeSqlList = sqlParseUtils.getSqls(sqlEntity.getSql(), false);
            if (!CollectionUtils.isEmpty(executeSqlList)) {
                executeSqlList.forEach(sql -> sqlUtils.executeSensitive(sql));
            }
            return "执行成功";
        } catch(Exception e) {
            return e.getMessage();
        }
    }

    public List<PaginateWithQueryColumns> executeSql(ViewExecuteSql executeSql) throws NotFoundException, ServerException {
        List<PaginateWithQueryColumns> list = new ArrayList<>();
        Connection source = connectionService.getSource(executeSql.getSourceId());
        // 结构化Sql
        try {
            SqlEntity sqlEntity = sqlParseUtils.parseSql(executeSql.getSql(),  sqlTempDelimiter);
            if (null == sqlUtils || null == sqlEntity || StringUtils.isEmpty(sqlEntity.getSql())) {
                return list;
            }
            if (!CollectionUtils.isEmpty(sqlEntity.getQuaryParams())) {
                sqlEntity.getQuaryParams().forEach((k, v) -> {
                    if (v instanceof List && ((List) v).size() > 0) {
                        v = ((List) v).stream().collect(Collectors.joining(COMMA)).toString();
                    }
                    sqlEntity.getQuaryParams().put(k, v);
                });
            }
            SqlUtils sqlUtils = this.sqlUtils.init(source);
            List<String> executeSqlList = sqlParseUtils.getSqls(sqlEntity.getSql(), false);
            List<String> querySqlList = sqlParseUtils.getSqls(sqlEntity.getSql(), true);
            if (!CollectionUtils.isEmpty(executeSqlList)) {
                executeSqlList.forEach(sql -> sqlUtils.execute(sql));
            }
            if (!CollectionUtils.isEmpty(querySqlList)) {
                for (String sql : querySqlList) {
                    sql = SqlParseUtils.rebuildSqlWithFragment(sql);
                    PaginateWithQueryColumns paginateWithQueryColumns = sqlUtils.syncQuery4Paginate(sql, null, null, null, executeSql.getLimit(),null);
                    list.add(paginateWithQueryColumns);
                }
            }
        } catch (Exception e) {
            throw new ServerException(e.getMessage());
        }
        return list;
    }

}
