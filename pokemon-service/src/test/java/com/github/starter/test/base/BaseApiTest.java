package com.github.starter.test.base;
import com.github.starter.test.categories.ApiTest;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Tag("api")
public abstract class BaseApiTest implements ApiTest {
    @Autowired
    protected TestRestTemplate restTemplate;
}
