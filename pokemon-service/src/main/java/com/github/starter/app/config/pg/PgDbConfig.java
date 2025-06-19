package com.github.starter.app.config.pg;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "core", name = "db", havingValue = "pg")
public class PgDbConfig {
    @Bean
    public ConnectionFactory connectionFactory(PgParams pgParams) {
        PostgresqlConnectionConfiguration configuration = PostgresqlConnectionConfiguration.builder()
                .host(pgParams.getHost())
                .port(pgParams.getPort())
                .database(pgParams.getDatabase())
                .username(pgParams.getUsername())
                .password(pgParams.getPassword())
                .build();
        return new PostgresqlConnectionFactory(configuration);
    }
    @Bean
    public DatabaseClient databaseClient(ConnectionFactory connectionFactory) {
        return DatabaseClient.create(connectionFactory);
    }
    @Bean
    public R2dbcEntityTemplate r2dbcEntityTemplate(ConnectionFactory connectionFactory) {
        return new R2dbcEntityTemplate(connectionFactory);
    }
}
