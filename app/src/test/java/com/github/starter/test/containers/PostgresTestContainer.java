package com.github.starter.test.containers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
public class PostgresTestContainer {
    private static final String IMAGE_VERSION = "postgres:16-alpine";
    private static PostgreSQLContainer<?> container;
    private PostgresTestContainer() {
    }
    public static PostgreSQLContainer<?> getInstance() {
        if (container == null) {
            container = new PostgreSQLContainer<>(DockerImageName.parse(IMAGE_VERSION))
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test")
                .withReuse(true);
        }
        return container;
    }
    public static String getJdbcUrl() {
        return container.getJdbcUrl();
    }
    public static String getUsername() {
        return container.getUsername();
    }
    public static String getPassword() {
        return container.getPassword();
    }
}
