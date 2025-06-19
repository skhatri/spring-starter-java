package com.github.starter.test.base;
import com.github.starter.Application;
import com.github.starter.test.categories.IntegrationTest;
import com.github.starter.test.containers.PostgresTestContainer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
@Tag("database")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {Application.class})
@Testcontainers
@ActiveProfiles("test")
public abstract class BaseDatabaseTest implements IntegrationTest {
    protected static final PostgreSQLContainer<?> postgres = PostgresTestContainer.getInstance();
    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", PostgresTestContainer::getJdbcUrl);
        registry.add("spring.datasource.username", PostgresTestContainer::getUsername);
        registry.add("spring.datasource.password", PostgresTestContainer::getPassword);
    }
}
