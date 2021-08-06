package com.biubiu.dms.controller;

import com.biubiu.dms.core.annotation.SystemLog;
import com.biubiu.dms.core.model.TableInfo;
import com.biubiu.dms.dto.Node;
import com.biubiu.dms.model.DBTables;
import com.biubiu.dms.pojo.Connection;
import com.biubiu.dms.service.ConnectionService;
import com.biubiu.dms.service.MetaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "connection", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class ConnectionController {


    private final static Logger logger = LoggerFactory.getLogger(ConnectionController.class);

    @Autowired
    private ConnectionService connectionService;

    @Resource
    private MetaService metaService;

    @SystemLog(description = "获取列表")
    @GetMapping(value = "")
    public ResponseEntity<?> getConnectionList() {
        try {
            List<Node> list = connectionService.getConnectionList("standard");
            return new ResponseEntity<>(list, HttpStatus.OK);
        } catch(Exception e) {
            logger.error("", e);
        }
        return new ResponseEntity<>(new ArrayList<>(),HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @SystemLog(description = "创建连接")
    @PostMapping(value = "")
    public ResponseEntity<?> insert(@RequestBody Connection connection) {
        try {
            int count = connectionService.insert(connection);
            if(count > 0) {
                return new ResponseEntity<>(connection.getId(), HttpStatus.OK);
            }
            return new ResponseEntity<>(0, HttpStatus.OK);
        } catch(Exception e) {
            logger.error("", e);
        }
        return new ResponseEntity<>(0, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @SystemLog(description = "更新连接")
    @PutMapping(value = "")
    public ResponseEntity<?> update(@RequestBody Connection connection) {
        try {
            int count = connectionService.update(connection);
            if(count > 0) {
                return new ResponseEntity<>(connection.getId(), HttpStatus.OK);
            }
            return new ResponseEntity<>(0, HttpStatus.OK);
        } catch(Exception e) {
            logger.error("", e);
        }
        return new ResponseEntity<>(0, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @SystemLog(description = "删除连接")
    @DeleteMapping(value = "")
    public ResponseEntity<?> delete(Long id) {
        try {
            int count = connectionService.delete(id);
            if(count > 0) {
                return new ResponseEntity<>(id, HttpStatus.OK);
            }
            return new ResponseEntity<>(0, HttpStatus.OK);
        } catch(Exception e) {
            logger.error("", e);
        }
        return new ResponseEntity<>(0, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @SystemLog(description = "获取链接的所有数据库")
    @GetMapping(value = "schema")
    public ResponseEntity<?> getSourceSchema(@RequestParam(name = "id") Long id) {
        try {
            List<String> schemas = metaService.getSchemas(id);
            return new ResponseEntity<>(schemas, HttpStatus.OK);
        } catch(Exception e) {
            logger.error("", e);
        }
        return new ResponseEntity<>(new ArrayList<>(),HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @SystemLog(description = "获取制定数据库的所有表")
    @GetMapping(value = "/schema/tables")
    public ResponseEntity<?> getSourceTable(@RequestParam(name = "id") Long id, @RequestParam(name = "schema") String schema) {
        try {
            DBTables dbTables = metaService.getSourceTables(id, schema);
            return new ResponseEntity<>(dbTables, HttpStatus.OK);
        } catch(Exception e) {
            logger.error("", e);
        }
        return new ResponseEntity<>(new ArrayList<>(),HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @SystemLog(description = "获取制定数据库制定表下的所有字段")
    @GetMapping(value = "/schema/table/fields")
    public ResponseEntity<?> getSourceField(@RequestParam(name = "id") Long id, @RequestParam(name = "schema") String schema, @RequestParam(name = "table") String table) {
        try {
            TableInfo tableInfo = metaService.getTableInfo(id, schema, table);
            return new ResponseEntity<>(tableInfo, HttpStatus.OK);
        } catch(Exception e) {
            logger.error("", e);
        }
        return new ResponseEntity<>(new ArrayList<>(),HttpStatus.INTERNAL_SERVER_ERROR);
    }


}
