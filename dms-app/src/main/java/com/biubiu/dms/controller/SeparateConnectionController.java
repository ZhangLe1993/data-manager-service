package com.biubiu.dms.controller;

import com.biubiu.dms.core.annotation.SystemLog;
import com.biubiu.dms.dto.Node;
import com.biubiu.dms.dto.SourceConfig;
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

/**
 * @author ：张音乐
 * @date ：Created in 2021/8/6 下午2:06
 * @description： 分离模式
 * @email: zhangyule1993@sina.com
 * @version:
 */
@RestController
@RequestMapping(value = "/separate/connection", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class SeparateConnectionController {

    private final static Logger logger = LoggerFactory.getLogger(SeparateConnectionController.class);

    @Autowired
    private ConnectionService connectionService;

    @Resource
    private MetaService metaService;

    @SystemLog(description = "获取列表")
    @GetMapping(value = "")
    public ResponseEntity<?> getConnectionList() {
        try {
            List<Node> list = connectionService.getConnectionList("separate");
            return new ResponseEntity<>(list, HttpStatus.OK);
        } catch(Exception e) {
            logger.error("", e);
        }
        return new ResponseEntity<>(new ArrayList<>(),HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @SystemLog(description = "测试实例")
    @PostMapping(value = "/test")
    public ResponseEntity<?> testSchema(@RequestBody SourceConfig sourceConfig) {
        try {
            boolean pass = metaService.testConnection(sourceConfig);
            if(pass) {
                return new ResponseEntity<>("测试通过", HttpStatus.OK);
            }
            return new ResponseEntity<>("无法联通", HttpStatus.OK);
        } catch(Exception e) {
            logger.error("", e);
        }
        return new ResponseEntity<>("服务异常",HttpStatus.INTERNAL_SERVER_ERROR);
    }

}