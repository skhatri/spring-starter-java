package com.github.starter.modules.core.endpoint;

import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
public class CoreEndpoint {

    @QueryMapping
    @GetMapping("/health")
    public Map<String, String> health() {
        return statusMap();
    }

    @SubscriptionMapping
    @GetMapping("/health-stream")
    public Flux<Map<String, String>> streamHealth() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(n -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("id", String.format("%d", n));
                    response.putAll(statusMap());
                    return response;
                });
    }

    @QueryMapping
    @GetMapping("/check")
    public Map<String, String> detailedHealthCheck() {
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        String status = "DOWN";
        String reason = "";

        try {
            String appData = Objects.requireNonNullElse(System.getenv("APP_DATA_PATH"), ".");
            File file = new File(String.format("%s/test.txt", appData));
            if (file.exists() || file.createNewFile()) {
                Files.write(file.toPath(), String.format("last check %s", now).getBytes());
            }
            status = "UP";
        } catch (IOException e) {
            reason = e.getMessage();
        }

        Map<String, String> response = new HashMap<>(statusMap());
        response.put("status", status);
        response.put("reason", reason);
        return response;
    }

    private Map<String, String> statusMap() {
        return Map.of(
                "status", "UP",
                "server_time", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
        );
    }
}
