package com.certificate.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class DataSourceUrlFixer {

    private static final Logger log = LoggerFactory.getLogger(DataSourceUrlFixer.class);

    @Bean
    public static BeanPostProcessor dataSourcePropertyFixer() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof DataSourceProperties) {
                    DataSourceProperties properties = (DataSourceProperties) bean;
                    String url = properties.getUrl();

                    if (url != null) {
                        // Fix common Render/Cloud provider issue where "jdbc:" is missing
                        if (url.startsWith("postgres://")) {
                            String fixedUrl = "jdbc:" + url;
                            log.info("Fixing JDBC URL: Prepending 'jdbc:' to 'postgres://' URL");
                            properties.setUrl(fixedUrl);
                        } else if (url.startsWith("postgresql://")) {
                            String fixedUrl = "jdbc:" + url;
                            log.info("Fixing JDBC URL: Prepending 'jdbc:' to 'postgresql://' URL");
                            properties.setUrl(fixedUrl);
                        }
                    }
                }
                return bean;
            }
        };
    }
}
