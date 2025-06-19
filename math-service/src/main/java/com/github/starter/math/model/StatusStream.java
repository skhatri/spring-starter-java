package com.github.starter.math.model;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

public record StatusStream(
    String id,
    String status,
    String server_time
) {
    public static StatusStream create(long sequenceId) {
        return new StatusStream(
            String.valueOf(sequenceId),
            "UP",
            DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        );
    }
} 