package com.github.starter.math.model;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CalculationResponse(
    @JsonProperty("result") double result,
    @JsonProperty("operation") Operation operation,
    @JsonProperty("operands") Operands operands,
    @JsonProperty("timestamp") Instant timestamp
) {
    
    public static CalculationResponse of(double result, CalculationRequest request) {
        return new CalculationResponse(
            result,
            request.operation(),
            new Operands(request.number1(), request.number2()),
            Instant.now()
        );
    }
    
    public record Operands(
        @JsonProperty("number1") double number1,
        @JsonProperty("number2") double number2
    ) {}
} 