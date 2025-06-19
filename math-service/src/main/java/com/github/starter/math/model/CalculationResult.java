package com.github.starter.math.model;

public record CalculationResult(
    double operand1,
    double operand2,
    Operation operation,
    double result,
    String expression
) {
    public static CalculationResult fromResponse(CalculationResponse response) {
        String expression = String.format("%.1f %s %.1f = %.1f", 
            response.operands().number1(), 
            getOperationSymbol(response.operation()), 
            response.operands().number2(), 
            response.result());
            
        return new CalculationResult(
            response.operands().number1(),
            response.operands().number2(),
            response.operation(),
            response.result(),
            expression
        );
    }
    
    private static String getOperationSymbol(Operation operation) {
        return switch (operation) {
            case ADD -> "+";
            case SUBTRACT -> "-";
            case MULTIPLY -> "×";
            case DIVIDE -> "÷";
        };
    }
} 