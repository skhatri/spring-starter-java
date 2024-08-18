package com.github.starter.modules.core.endpoint;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
public class CoreEndpoint {


    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("server_time", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        return response;
    }

    @GetMapping("/check")
    public Map<String, String> detailedHealthCheck() {
        Map<String, String> response = new HashMap<>();
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        String status = "UP";
        String reason = "";

        try {
            String appData = System.getenv("APP_DATA_PATH");
            File file = new File(appData + "/test.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            Files.write(file.toPath(), ("last check " + now).getBytes());
        } catch (IOException e) {
            reason = e.getMessage();
            status = "DOWN";
        }

        response.put("status", status);
        response.put("server_time", now);
        response.put("reason", reason);
        return response;
    }
}
