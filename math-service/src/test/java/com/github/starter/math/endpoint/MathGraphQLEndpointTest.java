package com.github.starter.math.endpoint;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.starter.math.model.CalculationHistory;
import com.github.starter.math.model.CalculationInput;
import com.github.starter.math.model.CalculationRequest;
import com.github.starter.math.model.CalculationResponse;
import com.github.starter.math.model.CalculationResult;
import com.github.starter.math.model.Operation;
import com.github.starter.math.model.Status;
import com.github.starter.math.model.StatusStream;
import com.github.starter.math.service.CalculationService;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("MathGraphQLEndpoint Tests")
class MathGraphQLEndpointTest {

    @Mock
    private CalculationService calculationService;

    @Mock
    private Tracer tracer;

    @Mock
    private SpanBuilder spanBuilder;

    @Mock
    private Span span;

    private MathGraphQLEndpoint endpoint;

    @BeforeEach
    void setUp() {
        lenient().when(tracer.spanBuilder(anyString())).thenReturn(spanBuilder);
        lenient().when(spanBuilder.setAttribute(anyString(), anyString())).thenReturn(spanBuilder);
        lenient().when(spanBuilder.setAttribute(anyString(), any(Double.class))).thenReturn(spanBuilder);
        lenient().when(spanBuilder.setAttribute(anyString(), any(Long.class))).thenReturn(spanBuilder);
        lenient().when(spanBuilder.setAttribute(anyString(), any(Integer.class))).thenReturn(spanBuilder);
        lenient().when(spanBuilder.startSpan()).thenReturn(span);
        
        endpoint = new MathGraphQLEndpoint(calculationService, tracer);
    }

    @Test
    @DisplayName("Should return status")
    void shouldReturnStatus() {
        Status result = endpoint.getStatus();

        assertNotNull(result);
        assertEquals("UP", result.status());
        assertNotNull(result.server_time());
        verify(tracer).spanBuilder("math.status.graphql");
    }

    @Test
    @DisplayName("Should calculate addition via GraphQL")
    void shouldCalculateAdditionViaGraphQL() {
        CalculationInput input = new CalculationInput(5.0, 3.0, Operation.ADD);
        CalculationResponse response = createMockResponse(8.0, input);

        when(calculationService.calculate(any(CalculationRequest.class)))
            .thenReturn(response);

        Mono<CalculationResult> result = endpoint.calculate(input);

        StepVerifier.create(result)
            .assertNext(calculationResult -> {
                assertNotNull(calculationResult);
                assertEquals(5.0, calculationResult.operand1());
                assertEquals(3.0, calculationResult.operand2());
                assertEquals(Operation.ADD, calculationResult.operation());
                assertEquals(8.0, calculationResult.result());
                assertEquals("5.0 + 3.0 = 8.0", calculationResult.expression());
            })
            .verifyComplete();

        verify(calculationService).calculate(any(CalculationRequest.class));
        verify(tracer).spanBuilder("math.calculate.graphql");
    }

    @Test
    @DisplayName("Should calculate division via GraphQL")
    void shouldCalculateDivisionViaGraphQL() {
        CalculationInput input = new CalculationInput(15.0, 3.0, Operation.DIVIDE);
        CalculationResponse response = createMockResponse(5.0, input);

        when(calculationService.calculate(any(CalculationRequest.class)))
            .thenReturn(response);

        Mono<CalculationResult> result = endpoint.calculate(input);

        StepVerifier.create(result)
            .assertNext(calculationResult -> {
                assertNotNull(calculationResult);
                assertEquals(15.0, calculationResult.operand1());
                assertEquals(3.0, calculationResult.operand2());
                assertEquals(Operation.DIVIDE, calculationResult.operation());
                assertEquals(5.0, calculationResult.result());
                assertEquals("15.0 ÷ 3.0 = 5.0", calculationResult.expression());
            })
            .verifyComplete();

        verify(calculationService).calculate(any(CalculationRequest.class));
    }

    @Test
    @DisplayName("Should handle division by zero error in GraphQL")
    void shouldHandleDivisionByZeroErrorInGraphQL() {
        CalculationInput input = new CalculationInput(10.0, 0.0, Operation.DIVIDE);

        when(calculationService.calculate(any(CalculationRequest.class)))
            .thenThrow(new ArithmeticException("Division by zero is not allowed"));

        Mono<CalculationResult> result = endpoint.calculate(input);

        StepVerifier.create(result)
            .expectError(ArithmeticException.class)
            .verify();

        verify(calculationService).calculate(any(CalculationRequest.class));
    }

    @Test
    @DisplayName("Should return supported operations")
    void shouldReturnSupportedOperations() {
        List<Operation> result = endpoint.getSupportedOperations();

        assertNotNull(result);
        assertEquals(4, result.size());
        assertTrue(result.contains(Operation.ADD));
        assertTrue(result.contains(Operation.SUBTRACT));
        assertTrue(result.contains(Operation.MULTIPLY));
        assertTrue(result.contains(Operation.DIVIDE));
        verify(tracer).spanBuilder("math.supported_operations.graphql");
    }

    @Test
    @DisplayName("Should perform calculation with history via GraphQL mutation")
    void shouldPerformCalculationWithHistoryViaGraphQLMutation() {
        CalculationInput input = new CalculationInput(7.0, 2.0, Operation.MULTIPLY);
        CalculationResponse response = createMockResponse(14.0, input);

        when(calculationService.calculate(any(CalculationRequest.class)))
            .thenReturn(response);

        Mono<CalculationHistory> result = endpoint.performCalculation(input);

        StepVerifier.create(result)
            .assertNext(history -> {
                assertNotNull(history);
                assertNotNull(history.id());
                assertNotNull(history.timestamp());
                
                CalculationResult calculation = history.calculation();
                assertNotNull(calculation);
                assertEquals(7.0, calculation.operand1());
                assertEquals(2.0, calculation.operand2());
                assertEquals(Operation.MULTIPLY, calculation.operation());
                assertEquals(14.0, calculation.result());
                assertEquals("7.0 × 2.0 = 14.0", calculation.expression());
            })
            .verifyComplete();

        verify(calculationService).calculate(any(CalculationRequest.class));
        verify(tracer).spanBuilder("math.perform_calculation.graphql");
    }

    @Test
    @DisplayName("Should stream health status")
    void shouldStreamHealthStatus() {
        Flux<StatusStream> result = endpoint.streamHealth();

        StepVerifier.create(result.take(3))
            .assertNext(status -> {
                assertNotNull(status);
                assertEquals("UP", status.status());
                assertEquals("0", status.id());
                assertNotNull(status.server_time());
            })
            .assertNext(status -> {
                assertNotNull(status);
                assertEquals("UP", status.status());
                assertEquals("1", status.id());
                assertNotNull(status.server_time());
            })
            .assertNext(status -> {
                assertNotNull(status);
                assertEquals("UP", status.status());
                assertEquals("2", status.id());
                assertNotNull(status.server_time());
            })
            .verifyComplete();
    }

    private CalculationResponse createMockResponse(double result, CalculationInput input) {
        CalculationRequest request = input.toCalculationRequest();
        return CalculationResponse.of(result, request);
    }
} 