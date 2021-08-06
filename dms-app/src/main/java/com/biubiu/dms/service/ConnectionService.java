package com.biubiu.dms.service;

import com.alibaba.fastjson.JSONObject;
import com.biubiu.dms.controller.ConnectionController;
import com.biubiu.dms.core.consts.Consts;
import com.biubiu.dms.core.exception.NotFoundException;
import com.biubiu.dms.dao.ConnectionDao;
import com.biubiu.dms.dto.Node;
import com.biubiu.dms.pojo.Connection;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Service
public class ConnectionService {

    private final static Logger logger = LoggerFactory.getLogger(ConnectionService.class);

    @Resource
    private ConnectionDao connectionDao;

    public List<Node> getConnectionList(String mode) {
        List<Connection> list = connectionDao.list();
        List<Node> target = new ArrayList<>();
        for (Connection connection : list) {
            String cMode = connection.getMode();
            if(cMode.equals(mode)) {
                Node node = new Node();
                Long id = connection.getId();
                node.setId(id);
                node.setName(connection.getName());
                node.setType("connection");
                node.setIcon(Consts.CONNECTION_ICON);
                node.setChildren(new ArrayList<>());
                JSONObject json = JSONObject.parseObject(connection.getConfig());
                if("separate".equals(mode)) {
                    String uri = json.getString("url");
                    String pre = StringUtils.substringBefore(uri, "?");
                    String schema = StringUtils.substringAfterLast(pre, "/");
                    node.setSchema(schema);
                    node.setUrl(uri);
                }
                node.setUsername(json.getString("username"));
                target.add(node);
            }
        }
        return target;
    }

    public Connection getSource(Long id) {
        Connection source = connectionDao.findById(id);
        if (null == source) {
            logger.error("source (:{}) not found", id);
            throw new NotFoundException("source is not found");
        }
        return source;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int insert(Connection connection) {
        return connectionDao.insert(connection);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int update(Connection connection) {
        return connectionDao.update(connection);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int delete(Long id) {
        return connectionDao.batchDelete(new HashSet<>(Collections.singletonList(id)));
    }

}
