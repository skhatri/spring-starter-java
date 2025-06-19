package com.github.starter.test.containers;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public final class PostgresTestContainer {
    
    private static final String IMAGE_VERSION = "postgres:16-alpine";
    private static volatile PostgreSQLContainer<?> container;
    
    private PostgresTestContainer() {
    }
    
    public static PostgreSQLContainer<?> getInstance() {
        PostgreSQLContainer<?> result = container;
        if (result == null) {
            synchronized (PostgresTestContainer.class) {
                result = container;
                if (result == null) {
                    container = result = createContainer();
                }
            }
        }
        return result;
    }
    
    @SuppressWarnings("resource")
    private static PostgreSQLContainer<?> createContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse(IMAGE_VERSION))
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);
    }
    
    public static String getJdbcUrl() {
        return getInstance().getJdbcUrl();
    }
    
    public static String getUsername() {
        return getInstance().getUsername();
    }
    
    public static String getPassword() {
        return getInstance().getPassword();
    }
}
