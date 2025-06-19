package com.github.starter.math.model;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

public record Status(
    String status,
    String server_time
) {
    public static Status up() {
        return new Status(
            "UP",
            DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        );
    }
    
    public static Status down() {
        return new Status(
            "DOWN",
            DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        );
    }
} 