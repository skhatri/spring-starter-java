package com.github.starter.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import static io.r2dbc.spi.ConnectionFactoryOptions.DATABASE;
import static io.r2dbc.spi.ConnectionFactoryOptions.DRIVER;
import static io.r2dbc.spi.ConnectionFactoryOptions.HOST;
import static io.r2dbc.spi.ConnectionFactoryOptions.PASSWORD;
import static io.r2dbc.spi.ConnectionFactoryOptions.PORT;
import static io.r2dbc.spi.ConnectionFactoryOptions.USER;

@TestConfiguration
@Testcontainers
public class PostgreSQLTestConfiguration {
    
    @Container
    static PostgreSQLContainer<?> postgres = createPostgresContainer();
    
    @SuppressWarnings("resource")
    private static PostgreSQLContainer<?> createPostgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:17.0"))
            .withDatabaseName("starter")
            .withUsername("postgres")
            .withPassword("password")
            .withCopyFileToContainer(
                MountableFile.forClasspathResource("db/csv/country.csv"), 
                "/docker-entrypoint-initdb.d/csv/country.csv"
            )
            .withCopyFileToContainer(
                MountableFile.forClasspathResource("db/csv/pokemon.csv"), 
                "/docker-entrypoint-initdb.d/csv/pokemon.csv"
            )
            .withCopyFileToContainer(
                MountableFile.forClasspathResource("db/csv/effectiveness.csv"), 
                "/docker-entrypoint-initdb.d/csv/effectiveness.csv"
            )
            .withInitScript("db/init-combined.sql")
            .withReuse(false);
    }
    
    static {
        postgres.start();
    }
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> String.format(
            "r2dbc:postgresql://%s:%d/%s",
            postgres.getHost(),
            postgres.getFirstMappedPort(),
            postgres.getDatabaseName()
        ));
        registry.add("spring.r2dbc.username", () -> "starter_user");
        registry.add("spring.r2dbc.password", () -> "starter");
        
        registry.add("spring.sql.init.mode", () -> "never");
        
        registry.add("logging.level.org.testcontainers", () -> "INFO");
        registry.add("logging.level.org.postgresql", () -> "DEBUG");
    }
    
    @Bean
    @Primary
    public ConnectionFactory testConnectionFactory() {
        return ConnectionFactories.get(ConnectionFactoryOptions.builder()
            .option(DRIVER, "postgresql")
            .option(HOST, postgres.getHost())
            .option(PORT, postgres.getFirstMappedPort())
            .option(DATABASE, postgres.getDatabaseName())
            .option(USER, "starter_user")
            .option(PASSWORD, "starter")
            .build());
    }
    
    public static String getContainerHost() {
        return postgres.getHost();
    }
    
    public static Integer getContainerPort() {
        return postgres.getFirstMappedPort();
    }
    
    public static String getContainerDatabaseName() {
        return postgres.getDatabaseName();
    }
} 