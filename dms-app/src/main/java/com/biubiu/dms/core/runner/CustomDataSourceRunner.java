package com.biubiu.dms.core.runner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

/*@Order(1)
@Component
@Slf4j*/
public class CustomDataSourceRunner implements ApplicationRunner {
    /*@Value("${custom-datasource-driver-path}")
    private String dataSourceYamlPath;*/

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void run(ApplicationArguments args) {
        try {
            // CustomDataSourceUtils.loadAllFromYaml(dataSourceYamlPath);
        } catch (Exception e) {
            // log.error("{}", e.getMessage());
            SpringApplication.exit(applicationContext);
            // log.info("Server shutdown");
        }
        // log.info("Load custom datasource finish");
    }
}
