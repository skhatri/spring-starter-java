package com.github.starter.math.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record CalculationRequest(
    @JsonProperty("number1") double number1,
    @JsonProperty("number2") double number2, 
    @JsonProperty("operation") Operation operation
) {
    
    @JsonCreator
    public CalculationRequest {
        if (operation == null) {
            throw new IllegalArgumentException("Operation cannot be null");
        }
        if (Double.isNaN(number1) || Double.isInfinite(number1)) {
            throw new IllegalArgumentException("Number1 must be a valid finite number");
        }
        if (Double.isNaN(number2) || Double.isInfinite(number2)) {
            throw new IllegalArgumentException("Number2 must be a valid finite number");
        }
    }
    
    public static CalculationRequest of(double number1, double number2, Operation operation) {
        return new CalculationRequest(number1, number2, operation);
    }
    
    public static CalculationRequest of(double number1, double number2, String operationStr) {
        return new CalculationRequest(number1, number2, Operation.fromString(operationStr));
    }
} 