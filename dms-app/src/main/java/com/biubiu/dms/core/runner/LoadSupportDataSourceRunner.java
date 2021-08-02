package com.biubiu.dms.core.runner;


import com.biubiu.dms.core.consts.Consts;
import com.biubiu.dms.core.enums.DataTypeEnum;
import com.biubiu.dms.core.model.DatasourceType;
import com.biubiu.dms.core.utils.CustomDataSourceUtils;
import lombok.Getter;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;

@Order(3)
@Component

public class LoadSupportDataSourceRunner implements ApplicationRunner {
    @Getter
    private static final List<DatasourceType> supportDatasourceList = new ArrayList<>();

    @Getter
    private static final Map<String, String> supportDatasourceMap = new HashMap<>();

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Map<String, List<String>> dataSourceVersoins = CustomDataSourceUtils.getDataSourceVersoin();

        for (DataTypeEnum dataTypeEnum : DataTypeEnum.values()) {
            if (dataSourceVersoins.containsKey(dataTypeEnum.getFeature())) {
                List<String> versions = dataSourceVersoins.get(dataTypeEnum.getFeature());
                if (!versions.isEmpty() && !versions.contains(Consts.JDBC_DATASOURCE_DEFAULT_VERSION)) {
                    versions.add(0, Consts.JDBC_DATASOURCE_DEFAULT_VERSION);
                }
            } else {
                dataSourceVersoins.put(dataTypeEnum.getFeature(), null);
            }
        }

        dataSourceVersoins.forEach((name, versions) -> supportDatasourceList.add(new DatasourceType(name, versions)));

        supportDatasourceList.forEach(s -> supportDatasourceMap.put(
                s.getName(),
                s.getName().equalsIgnoreCase(DataTypeEnum.ORACLE.getFeature()) ? Consts.ORACLE_JDBC_PREFIX : String.format(Consts.JDBC_PREFIX_FORMATER, s.getName())
        ));

        supportDatasourceList.sort(Comparator.comparing(DatasourceType::getName));
    }
}
