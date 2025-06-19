package com.github.starter.math.endpoint;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;

import com.github.starter.math.model.CalculationHistory;
import com.github.starter.math.model.CalculationInput;
import com.github.starter.math.model.CalculationRequest;
import com.github.starter.math.model.CalculationResult;
import com.github.starter.math.model.Operation;
import com.github.starter.math.model.Status;
import com.github.starter.math.model.StatusStream;
import com.github.starter.math.service.CalculationService;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class MathGraphQLEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(MathGraphQLEndpoint.class);
    
    private final CalculationService calculationService;
    private final Tracer tracer;

    public MathGraphQLEndpoint(CalculationService calculationService, Tracer tracer) {
        this.calculationService = calculationService;
        this.tracer = tracer;
    }

    @QueryMapping(name = "status")
    @WithSpan("math.status.graphql")
    public Status getStatus() {
        Span span = tracer.spanBuilder("math.status.graphql").startSpan();
        
        logger.info("GraphQL status query");
        
        Status status = Status.up();
        
        span.setAttribute("math.status", status.status());
        span.end();
        
        return status;
    }

    @QueryMapping(name = "calculate")
    @WithSpan("math.calculate.graphql")
    public Mono<CalculationResult> calculate(@Argument("input") CalculationInput input) {
        Span span = tracer.spanBuilder("math.calculate.graphql")
                .setAttribute("math.operation", input.operation().name())
                .setAttribute("math.operand1", input.operand1())
                .setAttribute("math.operand2", input.operand2())
                .startSpan();
        
        logger.info("GraphQL calculate - operation: {}, operand1: {}, operand2: {}", 
                   input.operation(), input.operand1(), input.operand2());
        
        CalculationRequest request = input.toCalculationRequest();
        
        return Mono.fromCallable(() -> calculationService.calculate(request))
                .map(response -> CalculationResult.fromResponse(response))
                .doOnNext(result -> {
                    span.setAttribute("math.result", result.result());
                    span.setAttribute("math.expression", result.expression());
                })
                .doOnError(error -> {
                    span.recordException(error);
                    span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, error.getMessage());
                })
                .doFinally(signalType -> span.end());
    }

    @QueryMapping(name = "supportedOperations")
    @WithSpan("math.supported_operations.graphql")
    public List<Operation> getSupportedOperations() {
        Span span = tracer.spanBuilder("math.supported_operations.graphql").startSpan();
        
        logger.info("GraphQL supportedOperations query");
        
        List<Operation> operations = Arrays.asList(Operation.values());
        
        span.setAttribute("math.operations_count", operations.size());
        span.end();
        
        return operations;
    }

    @MutationMapping(name = "performCalculation")
    @WithSpan("math.perform_calculation.graphql")
    public Mono<CalculationHistory> performCalculation(@Argument("input") CalculationInput input) {
        Span span = tracer.spanBuilder("math.perform_calculation.graphql")
                .setAttribute("math.operation", input.operation().name())
                .setAttribute("math.operand1", input.operand1())
                .setAttribute("math.operand2", input.operand2())
                .startSpan();
        
        logger.info("GraphQL performCalculation - operation: {}, operand1: {}, operand2: {}", 
                   input.operation(), input.operand1(), input.operand2());
        
        CalculationRequest request = input.toCalculationRequest();
        
        return Mono.fromCallable(() -> calculationService.calculate(request))
                .map(response -> CalculationHistory.fromResponse(response))
                .doOnNext(history -> {
                    span.setAttribute("math.history_id", history.id());
                    span.setAttribute("math.result", history.calculation().result());
                })
                .doOnError(error -> {
                    span.recordException(error);
                    span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, error.getMessage());
                })
                .doFinally(signalType -> span.end());
    }

    @SubscriptionMapping(name = "streamHealth")
    @WithSpan("math.stream_health.graphql")
    public Flux<StatusStream> streamHealth() {
        logger.info("GraphQL streamHealth subscription started");
        
        return Flux.interval(Duration.ofSeconds(1))
                .map(StatusStream::create);
    }
} 