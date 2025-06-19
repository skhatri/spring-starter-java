package com.github.starter;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import com.github.starter.config.PostgreSQLTestConfiguration;

@SpringBootTest
@Import(PostgreSQLTestConfiguration.class)
@TestPropertySource(properties = {
    "spring.sql.init.mode=never",
    "logging.level.org.testcontainers=INFO"
})
public abstract class PostgreSQLTestBase {
    
    @Test
    void shouldHavePostgreSQLContainerConfiguration() {
        String host = PostgreSQLTestConfiguration.getContainerHost();
        Integer port = PostgreSQLTestConfiguration.getContainerPort();
        String databaseName = PostgreSQLTestConfiguration.getContainerDatabaseName();
        
        assertNotNull(host, "PostgreSQL host should be available");
        assertNotNull(port, "PostgreSQL port should be available");
        assertNotNull(databaseName, "PostgreSQL database name should be available");
        assertTrue(port > 0, "PostgreSQL port should be positive");
    }
} 