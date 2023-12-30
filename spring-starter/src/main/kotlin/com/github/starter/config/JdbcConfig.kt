package com.github.starter.config

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.dialect.DialectResolver
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component


@Configuration
@Component
class JdbcConfig {

    @Autowired
    @Bean
    open fun jdbcClient(): DatabaseClient {

        val connFactory = PostgresqlConnectionFactory(
            PostgresqlConnectionConfiguration.builder()
                .host("localhost").port(5432)
                .database("pokemon")
                .username("pokemon_user")
                .password("pokemon").build()
        )

        return DatabaseClient.builder().connectionFactory(connFactory)
            .bindMarkers(DialectResolver.getDialect(connFactory).bindMarkersFactory)
            .build()
    }

}