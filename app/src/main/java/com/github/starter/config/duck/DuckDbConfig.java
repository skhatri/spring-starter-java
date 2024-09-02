package com.github.starter.config.duck;

import com.github.starter.exception.Exceptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;


@ConditionalOnProperty(prefix = "core", name = "db", havingValue = "duckdb")
@Configuration
@Component
public class DuckDbConfig {

    @Autowired
    @Bean
    public DataClient duck(DuckParams cfg) {
        Properties properties = new Properties();
        properties.setProperty("duckdb.read_only", String.valueOf(cfg.isReadOnly()));
        DataClient client = new DataClient(new SingleConnectionDataSource(cfg.getUrl(), false));
        if (cfg.getInit().isEnabled()) {
            try (FileInputStream fis = new FileInputStream(cfg.getInit().getFile())) {
                String lines = new String(fis.readAllBytes(), StandardCharsets.UTF_8);
                String[] stmts = lines.split(";");
                for (String stmt : stmts) {
                    if (!stmt.isBlank()) {
                        client.sql(stmt).execute();
                    }
                }
            } catch (Exception ex) {
                throw Exceptions.dbException(ex);
            }
        }
        return client;
    }
}
