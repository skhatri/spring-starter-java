package com.github.starter.core.exception;

import java.time.Instant;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@RestControllerAdvice
public class RestExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(RestExceptionHandler.class);
    
    @ExceptionHandler(AppException.class)
    public Mono<ResponseEntity<ErrorDto>> handleAppException(AppException ex, ServerWebExchange exchange) {
        logger.warn("Application exception: {}", ex.getMessage());
        
        ErrorDto error = new ErrorDto(
            HttpStatus.BAD_REQUEST.value(),
            "Application Error",
            ex.getMessage(),
            exchange.getRequest().getPath().value(),
            exchange.getRequest().getMethod().name(),
            exchange.getRequest().getId(),
            Instant.now(),
            Map.of("type", "application_error")
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error));
    }
    
    @ExceptionHandler(DatabaseException.class)
    public Mono<ResponseEntity<ErrorDto>> handleDatabaseException(DatabaseException ex, ServerWebExchange exchange) {
        logger.error("Database exception: {}", ex.getMessage(), ex);
        
        ErrorDto error = new ErrorDto(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Database Error",
            "A database error occurred",
            exchange.getRequest().getPath().value(),
            exchange.getRequest().getMethod().name(),
            exchange.getRequest().getId(),
            Instant.now(),
            Map.of("type", "database_error")
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error));
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorDto>> handleIllegalArgumentException(IllegalArgumentException ex, ServerWebExchange exchange) {
        logger.warn("Validation error: {}", ex.getMessage());
        
        ErrorDto error = new ErrorDto(
            HttpStatus.BAD_REQUEST.value(),
            "Invalid Request",
            ex.getMessage(),
            exchange.getRequest().getPath().value(),
            exchange.getRequest().getMethod().name(),
            exchange.getRequest().getId(),
            Instant.now(),
            Map.of("type", "validation_error")
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error));
    }
    
    @ExceptionHandler(RuntimeException.class)
    public Mono<ResponseEntity<ErrorDto>> handleRuntimeException(RuntimeException ex, ServerWebExchange exchange) {
        logger.error("Unexpected runtime exception: {}", ex.getMessage(), ex);
        
        ErrorDto error = new ErrorDto(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred",
            exchange.getRequest().getPath().value(),
            exchange.getRequest().getMethod().name(),
            exchange.getRequest().getId(),
            Instant.now(),
            Map.of(
                "type", "internal_server_error",
                "exception_class", ex.getClass().getSimpleName()
            )
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error));
    }
    
    public record ErrorDto(
        int status,
        String error,
        String message,
        String path,
        String method,
        String traceId,
        Instant timestamp,
        Map<String, Object> details
    ) {}
} 