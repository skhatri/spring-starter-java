package com.github.starter.config.pg;

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.dialect.DialectResolver;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;


@ConditionalOnProperty(prefix = "core", name="db", havingValue = "pg")
@Configuration
@Component
public class PgDbConfig {

    @Bean
    public DatabaseClient pgJdbcClient(PgParams params) {

        PostgresqlConnectionFactory connFactory = new PostgresqlConnectionFactory(
                PostgresqlConnectionConfiguration.builder()
                        .host(params.getHost())
                        .port(params.getPort())
                        .database(params.getDatabase())
                        .username(params.getUsername())
                        .password(params.getPassword())
                        .build()
        );

        return DatabaseClient.builder()
                .connectionFactory(connFactory)
                .bindMarkers(DialectResolver.getDialect(connFactory).getBindMarkersFactory())
                .build();
    }

}
