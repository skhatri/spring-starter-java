package com.github.starter.math.model;

public record CalculationInput(
    double operand1,
    double operand2,
    Operation operation
) {
    public CalculationInput {
        if (operation == null) {
            throw new IllegalArgumentException("Operation cannot be null");
        }
    }
    
    public CalculationRequest toCalculationRequest() {
        return new CalculationRequest(operand1, operand2, operation);
    }
} 