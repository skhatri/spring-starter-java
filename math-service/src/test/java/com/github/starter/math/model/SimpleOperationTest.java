package com.github.starter.math.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

class SimpleOperationTest {

    @Test
    void shouldPerformAddition() {
        double result = Operation.ADD.calculate(5.0, 3.0);
        assertThat(result).isEqualTo(8.0);
    }

    @Test
    void shouldPerformSubtraction() {
        double result = Operation.SUBTRACT.calculate(10.0, 4.0);
        assertThat(result).isEqualTo(6.0);
    }

    @Test
    void shouldPerformMultiplication() {
        double result = Operation.MULTIPLY.calculate(7.0, 2.0);
        assertThat(result).isEqualTo(14.0);
    }

    @Test
    void shouldPerformDivision() {
        double result = Operation.DIVIDE.calculate(15.0, 3.0);
        assertThat(result).isEqualTo(5.0);
    }

    @Test
    void shouldThrowExceptionForDivisionByZero() {
        assertThatThrownBy(() -> Operation.DIVIDE.calculate(10.0, 0.0))
            .isInstanceOf(ArithmeticException.class)
            .hasMessage("Division by zero is not allowed");
    }

    @Test
    void shouldHaveCorrectSymbols() {
        assertThat(Operation.ADD.getSymbol()).isEqualTo("+");
        assertThat(Operation.SUBTRACT.getSymbol()).isEqualTo("-");
        assertThat(Operation.MULTIPLY.getSymbol()).isEqualTo("*");
        assertThat(Operation.DIVIDE.getSymbol()).isEqualTo("/");
    }

    @Test
    void shouldValidateOperationFromString() {
        assertThat(Operation.fromString("ADD")).isEqualTo(Operation.ADD);
        assertThat(Operation.fromString("add")).isEqualTo(Operation.ADD);
    }

    @Test
    void shouldThrowExceptionForInvalidOperation() {
        assertThatThrownBy(() -> Operation.fromString("INVALID"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid operation: INVALID");
    }
} 