package com.github.starter.math.model;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

public record CalculationHistory(
    String id,
    CalculationResult calculation,
    String timestamp
) {
    public static CalculationHistory fromResponse(CalculationResponse response) {
        CalculationResult result = CalculationResult.fromResponse(response);
        return new CalculationHistory(
            UUID.randomUUID().toString(),
            result,
            DateTimeFormatter.ISO_INSTANT.format(response.timestamp())
        );
    }
} 