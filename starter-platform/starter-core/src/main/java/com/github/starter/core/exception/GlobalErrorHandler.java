package com.github.starter.core.exception;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@Component
@Order(-2)
public class GlobalErrorHandler implements ErrorWebExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalErrorHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        logger.error("Global error handler caught exception: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = createErrorResponse(ex, exchange);
        
        return exchange.getResponse()
            .writeWith(Mono.fromCallable(() -> {
                exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                exchange.getResponse().setStatusCode(errorResponse.status());
                
                try {
                    String json = objectMapper.writeValueAsString(errorResponse);
                    return exchange.getResponse().bufferFactory()
                        .wrap(json.getBytes(StandardCharsets.UTF_8));
                } catch (JsonProcessingException e) {
                    logger.error("Failed to serialize error response", e);
                    String fallback = "{\"error\":\"Internal server error\",\"message\":\"Failed to serialize error response\"}";
                    return exchange.getResponse().bufferFactory()
                        .wrap(fallback.getBytes(StandardCharsets.UTF_8));
                }
            }))
            .doOnError(writeError -> logger.error("Failed to write error response", writeError))
            .onErrorResume(writeError -> Mono.empty());
    }
    
    private ErrorResponse createErrorResponse(Throwable ex, ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();
        String traceId = exchange.getRequest().getId();
        
        if (ex instanceof AppException appEx) {
            return new ErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Application Error",
                appEx.getMessage(),
                path,
                method,
                traceId,
                Instant.now(),
                Map.of("type", "application_error")
            );
        }
        
        if (ex instanceof DatabaseException dbEx) {
            return new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Database Error",
                "A database error occurred",
                path,
                method,
                traceId,
                Instant.now(),
                Map.of("type", "database_error")
            );
        }
        
        if (ex instanceof IllegalArgumentException illegalArgEx) {
            return new ErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid Request",
                illegalArgEx.getMessage(),
                path,
                method,
                traceId,
                Instant.now(),
                Map.of("type", "validation_error")
            );
        }
        
        return new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            "An unexpected error occurred",
            path,
            method,
            traceId,
            Instant.now(),
            Map.of(
                "type", "internal_server_error",
                "exception_class", ex.getClass().getSimpleName()
            )
        );
    }
    
    public record ErrorResponse(
        HttpStatus status,
        String error,
        String message,
        String path,
        String method,
        String traceId,
        Instant timestamp,
        Map<String, Object> details
    ) {
        public int getStatus() {
            return status.value();
        }
        
        public String getError() {
            return error;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String getPath() {
            return path;
        }
        
        public String getMethod() {
            return method;
        }
        
        public String getTraceId() {
            return traceId;
        }
        
        public Instant getTimestamp() {
            return timestamp;
        }
        
        public Map<String, Object> getDetails() {
            return details;
        }
    }
} 