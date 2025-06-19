package com.github.starter.math.model;

import java.util.Locale;

public enum Operation {
    ADD("+") {
        @Override
        public double calculate(double number1, double number2) {
            return number1 + number2;
        }
    },
    SUBTRACT("-") {
        @Override
        public double calculate(double number1, double number2) {
            return number1 - number2;
        }
    },
    MULTIPLY("*") {
        @Override
        public double calculate(double number1, double number2) {
            return number1 * number2;
        }
    },
    DIVIDE("/") {
        @Override
        public double calculate(double number1, double number2) {
            if (number2 == 0.0) {
                throw new ArithmeticException("Division by zero is not allowed");
            }
            return number1 / number2;
        }
    };

    private final String symbol;

    Operation(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public abstract double calculate(double number1, double number2);

    public static Operation fromString(String operationStr) {
        if (operationStr == null || operationStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Operation cannot be null or empty");
        }
        
        try {
            return Operation.valueOf(operationStr.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid operation: " + operationStr);
        }
    }
} 