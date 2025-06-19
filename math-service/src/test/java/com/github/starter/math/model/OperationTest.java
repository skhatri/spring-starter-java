package com.github.starter.math.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OperationTest {

    @ParameterizedTest
    @CsvSource({
        "ADD, 5.0, 3.0, 8.0",
        "SUBTRACT, 10.0, 4.0, 6.0",
        "MULTIPLY, 7.0, 2.0, 14.0",
        "DIVIDE, 15.0, 3.0, 5.0",
        "DIVIDE, 7.0, 2.0, 3.5"
    })
    void shouldPerformCorrectCalculations(Operation operation, double number1, double number2, double expected) {
        double result = operation.calculate(number1, number2);
        
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldThrowExceptionForDivisionByZero() {
        assertThatThrownBy(() -> Operation.DIVIDE.calculate(10.0, 0.0))
            .isInstanceOf(ArithmeticException.class)
            .hasMessage("Division by zero is not allowed");
    }

    @ParameterizedTest
    @EnumSource(Operation.class)
    void shouldHaveValidStringRepresentation(Operation operation) {
        String stringValue = operation.toString();
        
        assertThat(stringValue).isNotBlank();
        assertThat(stringValue).isEqualTo(operation.name());
    }

    @ParameterizedTest
    @CsvSource({
        "ADD, +",
        "SUBTRACT, -", 
        "MULTIPLY, *",
        "DIVIDE, /"
    })
    void shouldHaveCorrectSymbol(Operation operation, String expectedSymbol) {
        String symbol = operation.getSymbol();
        
        assertThat(symbol).isEqualTo(expectedSymbol);
    }

    @Test
    void shouldValidateOperationFromString() {
        assertThat(Operation.fromString("ADD")).isEqualTo(Operation.ADD);
        assertThat(Operation.fromString("add")).isEqualTo(Operation.ADD);
        assertThat(Operation.fromString("Add")).isEqualTo(Operation.ADD);
    }

    @Test
    void shouldThrowExceptionForInvalidOperation() {
        assertThatThrownBy(() -> Operation.fromString("INVALID"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid operation: INVALID");
            
        assertThatThrownBy(() -> Operation.fromString(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Operation cannot be null or empty");
    }
} 