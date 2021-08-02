package com.biubiu.dms.pojo;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.biubiu.dms.core.model.BaseSource;
import com.biubiu.dms.core.model.Dict;
import com.biubiu.dms.core.utils.SourceUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.biubiu.dms.core.consts.Consts.JDBC_DATASOURCE_DEFAULT_VERSION;

@Slf4j
@Data
public class Connection extends BaseSource {
    private Long id;
    private String name;
    private String config;

    /**
     * 从config中获取jdbcUrl
     * <p>
     * json key： url
     *
     * @return
     */
    @Override
    @JSONField(serialize = false)
    public String getJdbcUrl() {
        String url = null;
        if (null == config) {
            return null;
        }
        try {
            JSONObject jsonObject = JSONObject.parseObject(this.config);
            String host = jsonObject.getString("host");
            Integer port = jsonObject.getInteger("port");
            // 以 mysql 为例子
            url = "jdbc:mysql://" + host + ":" + port;
        } catch (Exception e) {
            log.error("get jdbc url from source config, {}", e.getMessage());
        }
        return url;
    }

    /**
     * 从config中获取jdbc username
     * <p>
     * json key: user
     *
     * @return
     */
    @Override
    @JSONField(serialize = false)
    public String getUsername() {
        String username = null;
        if (null == config) {
            return null;
        }
        try {
            JSONObject jsonObject = JSONObject.parseObject(this.config);
            username = jsonObject.getString("username");
        } catch (Exception e) {
            log.error("get jdbc user from source config, {}", e.getMessage());
        }
        return username;
    }

    /**
     * 从config中获取 jdbc password
     * <p>
     * json key: password
     *
     * @return
     */
    @Override
    @JSONField(serialize = false)
    public String getPassword() {
        String password = null;
        if (null == config) {
            return null;
        }
        try {
            JSONObject jsonObject = JSONObject.parseObject(this.config);
            password = jsonObject.getString("password");
        } catch (Exception e) {
            log.error("get jdbc password from source config, {}", e.getMessage());
        }
        return password;
    }

    @Override
    @JSONField(serialize = false)
    public String getDatabase() {
        return SourceUtils.getDataSourceName(getJdbcUrl());
    }

    @Override
    @JSONField(serialize = false)
    public String getDbVersion() {
        String versoin = null;
        if (null == config) {
            return null;
        }
        try {
            JSONObject jsonObject = JSONObject.parseObject(this.config);
            versoin = jsonObject.getString("version");
            if (JDBC_DATASOURCE_DEFAULT_VERSION.equals(versoin)) {
                return null;
            }
        } catch (Exception e) {
        }
        return StringUtils.isEmpty(versoin) ? null : versoin;
    }

    @Override
    @JSONField(serialize = false)
    public boolean isExt() {
        boolean ext = false;
        if (null == config) {
            return false;
        }
        if (getDbVersion() == null) {
            ext = false;
        }
        try {
            JSONObject jsonObject = JSONObject.parseObject(this.config);
            ext = jsonObject.getBooleanValue("ext");
        } catch (Exception e) {
        }
        return ext;
    }

    @JSONField(serialize = false)
    public List<Dict> getProperties() {
        if (null == config) {
            return null;
        }
        List<Dict> dicts = null;
        try {
            JSONObject configObject = JSONObject.parseObject(this.config);
            if (configObject != null && configObject.containsKey("properties")) {
                JSONArray jsonArray = configObject.getJSONArray("properties");
                if (jsonArray != null && !jsonArray.isEmpty()) {
                    dicts = jsonArray.toJavaList(Dict.class);
                }
            }
        } catch (Exception e) {
            log.error("get jdbc properties from source config, {}", e.getMessage());
        }
        return dicts;
    }


    @JSONField(serialize = false)
    public String getConfigParams() {
        String params = null;
        if (null == config) {
            return null;
        }
        try {
            JSONObject jsonObject = JSONObject.parseObject(this.config);
            params = jsonObject.getString("parameters");
        } catch (Exception e) {
            log.error("get jdbc parameters from source config, {}", e.getMessage());
        }
        return params;
    }
}
