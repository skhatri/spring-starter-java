package com.github.starter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.github.starter.config.PostgreSQLTestConfiguration;

@SpringBootTest
@Testcontainers
@Import(PostgreSQLTestConfiguration.class)
@TestPropertySource(properties = {
    "spring.sql.init.mode=never",
    "logging.level.org.testcontainers=INFO"
})
public abstract class PostgreSQLTestBase {
    
    @Container
    static PostgreSQLContainer<?> postgres = PostgreSQLTestConfiguration.getPostgresContainer();
    
    @Test
    void shouldHavePostgreSQLContainerRunning() {
        assertTrue(postgres.isRunning(), "PostgreSQL testcontainer should be running");
        assertEquals("starter", postgres.getDatabaseName());
        assertEquals("postgres", postgres.getUsername());
        assertTrue(postgres.getFirstMappedPort() > 0, "PostgreSQL port should be mapped");
    }
    
    @Test
    void shouldHaveCorrectJdbcUrl() {
        String jdbcUrl = postgres.getJdbcUrl();
        assertNotNull(jdbcUrl);
        assertTrue(jdbcUrl.contains("postgresql"));
        assertTrue(jdbcUrl.contains("starter"));
    }
    
    @Test
    void shouldHavePostgreSQLVersion17() {
        String dockerImageName = postgres.getDockerImageName();
        assertTrue(dockerImageName.contains("postgres:17"));
    }
} 