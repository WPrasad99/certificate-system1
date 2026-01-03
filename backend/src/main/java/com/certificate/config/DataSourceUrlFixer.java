package com.certificate.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

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

                    if (url != null && (url.startsWith("postgres://") || url.startsWith("postgresql://"))) {
                        try {
                            log.info("Processing detected Cloud Database URL...");

                            // Normalized URI parsing
                            // Handle postgres:// scheme which might not be standard in all URI parsers
                            String uriString = url;
                            if (url.startsWith("postgres://")) {
                                uriString = url.replace("postgres://", "postgresql://");
                            }

                            URI uri = new URI(uriString);

                            // Extract user/pass if present
                            String userInfo = uri.getUserInfo();
                            if (userInfo != null) {
                                String[] parts = userInfo.split(":");
                                if (parts.length >= 1) {
                                    properties.setUsername(parts[0]);
                                    log.info("Extracted username from URL");
                                }
                                if (parts.length >= 2) {
                                    properties.setPassword(parts[1]);
                                    log.info("Extracted password from URL");
                                }
                            }

                            // Rebuild as valid JDBC URL (jdbc:postgresql://host:port/path)
                            String dbPath = uri.getPath();
                            if (dbPath == null || dbPath.isEmpty()) {
                                dbPath = "/postgres";
                            }

                            int port = uri.getPort();
                            if (port == -1)
                                port = 5432;

                            StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://");
                            jdbcUrl.append(uri.getHost());
                            jdbcUrl.append(":");
                            jdbcUrl.append(port);
                            jdbcUrl.append(dbPath);

                            // Append parameters (like sslmode)
                            String query = uri.getQuery();
                            if (query != null) {
                                jdbcUrl.append("?").append(query);
                            }

                            String finalUrl = jdbcUrl.toString();
                            log.info("Converted to safe JDBC URL: {}", finalUrl);
                            properties.setUrl(finalUrl);

                        } catch (URISyntaxException e) {
                            log.error("Failed to parse Database URL: {}", url, e);
                            // Fallback: just prepend jdbc: and hope for best
                            if (!url.startsWith("jdbc:")) {
                                properties.setUrl("jdbc:" + url);
                            }
                        }
                    }
                    // Other fix: simple prepend if missing
                    else if (url != null && !url.startsWith("jdbc:") && url.contains(":")) {
                        properties.setUrl("jdbc:" + url);
                    }
                }
                return bean;
            }
        };
    }
}
