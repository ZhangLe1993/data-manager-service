package com.biubiu.dms.core.utils;


import com.biubiu.dms.core.consts.Consts;
import com.biubiu.dms.core.model.CustomDataSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomDataSourceUtils {
    private static volatile Map<String, CustomDataSource> customDataSourceMap = new HashMap<>();

    @Getter
    private static volatile Map<String, List<String>> dataSourceVersoin = new HashMap<>();

    public static CustomDataSource getInstance(String jdbcUrl, String version) {
        String dataSourceName = SourceUtils.getDataSourceName(jdbcUrl);
        String key = getKey(dataSourceName, version);
        if (customDataSourceMap.containsKey(key) && null != customDataSourceMap.get(key)) {
            CustomDataSource customDataSource = customDataSourceMap.get(key);
            if (null != customDataSource) {
                return customDataSource;
            }
        }
        return null;
    }


    public static void loadAllFromYaml(String yamlPath) throws Exception {

        if (StringUtils.isEmpty(yamlPath)) {
            return;
        }

        File yamlFile = new File(yamlPath);
        if (!yamlFile.exists() || !yamlFile.isFile() || !yamlFile.canRead()) {
            return;
        }

        Yaml yaml = new Yaml();
        HashMap<String, Object> loads = yaml.loadAs(new BufferedReader(new FileReader(yamlFile)), HashMap.class);

        if (CollectionUtils.isEmpty(loads)) {
            return;
        }

        ObjectMapper mapper = new ObjectMapper();

        for (Map.Entry<String, Object> entry : loads.entrySet()) {

            CustomDataSource customDataSource = mapper.convertValue(entry.getValue(), CustomDataSource.class);

            if (StringUtils.isEmpty(customDataSource.getName()) || StringUtils.isEmpty(customDataSource.getDriver())) {
                throw new Exception("Load custom datasource error: name or driver cannot be EMPTY");
            }

            if ("null".equals(customDataSource.getName().trim().toLowerCase())) {
                throw new Exception("Load custom datasource error: invalid name");
            }

            if ("null".equals(customDataSource.getDriver().trim().toLowerCase())) {
                throw new Exception("Load custom datasource error: invalid driver");
            }

            if (StringUtils.isEmpty(customDataSource.getDesc())) {
                customDataSource.setDesc(customDataSource.getName());
            }

            if ("null".equals(customDataSource.getDesc().trim().toLowerCase())) {
                customDataSource.setDesc(customDataSource.getName());
            }

            if (!StringUtils.isEmpty(customDataSource.getKeyword_prefix()) || !StringUtils.isEmpty(customDataSource.getKeyword_suffix())) {
                if (StringUtils.isEmpty(customDataSource.getKeyword_prefix()) || StringUtils.isEmpty(customDataSource.getKeyword_suffix())) {
                    throw new Exception("Load custom datasource error: keyword prefixes and suffixes must be configured in pairs.");
                }
            }

            if (!StringUtils.isEmpty(customDataSource.getAlias_prefix()) || !StringUtils.isEmpty(customDataSource.getAlias_suffix())) {
                if (StringUtils.isEmpty(customDataSource.getAlias_prefix()) || StringUtils.isEmpty(customDataSource.getAlias_suffix())) {
                    throw new Exception("Load custom datasource error: alias prefixes and suffixes must be configured in pairs.");
                }
            }

            List<String> versoins = null;
            if (dataSourceVersoin.containsKey(customDataSource.getName())) {
                versoins = dataSourceVersoin.get(customDataSource.getName());
            } else {
                versoins = new ArrayList<>();
            }
            if (StringUtils.isEmpty(customDataSource.getVersion())) {
                versoins.add(0, Consts.JDBC_DATASOURCE_DEFAULT_VERSION);
            } else {
                versoins.add(customDataSource.getVersion());
            }

            if (versoins.size() == 1 && versoins.get(0).equals(Consts.JDBC_DATASOURCE_DEFAULT_VERSION)) {
                versoins.remove(0);
            }

            dataSourceVersoin.put(customDataSource.getName(), versoins);
            customDataSourceMap.put(getKey(customDataSource.getName(), customDataSource.getVersion()), customDataSource);
        }
    }

    private static String getKey(String database, String version) {
        return database + Consts.COLON + (StringUtils.isEmpty(version) ? Consts.EMPTY : version);
    }
}
