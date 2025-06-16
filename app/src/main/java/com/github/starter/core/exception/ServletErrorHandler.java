package com.github.starter.core.exception;

import java.time.Instant;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RestControllerAdvice
public class ServletErrorHandler implements ErrorController {

    private static final Logger logger = LoggerFactory.getLogger(ServletErrorHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @RequestMapping("/error")
    public ResponseEntity<ErrorResponse> handleError(HttpServletRequest request, HttpServletResponse response) {
        Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        String message = (String) request.getAttribute("jakarta.servlet.error.message");
        String requestUri = (String) request.getAttribute("jakarta.servlet.error.request_uri");
        Throwable exception = (Throwable) request.getAttribute("jakarta.servlet.error.exception");

        HttpStatus status = HttpStatus.resolve(statusCode);
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        String method = request.getMethod();
        String path = requestUri != null ? requestUri : request.getRequestURI();
        String traceId = extractTraceId(request);

        ErrorResponse errorResponse = new ErrorResponse(
            status.value(),
            status.getReasonPhrase(),
            formatMessage(status, message, path, method),
            path,
            method,
            traceId,
            Instant.now(),
            createDetails(status, exception)
        );

        logger.warn("HTTP {} error for {} {}: {}", 
            status.value(), method, path, errorResponse.message());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(NoHandlerFoundException ex, HttpServletRequest request) {
        String method = ex.getHttpMethod();
        String path = ex.getRequestURL();
        String traceId = extractTraceId(request);

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            String.format("No endpoint %s %s.", method, path),
            path,
            method,
            traceId,
            Instant.now(),
            Map.of("type", "no_handler_found")
        );

        logger.warn("No handler found for {} {}", method, path);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        String traceId = extractTraceId(request);

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
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

        logger.error("Unexpected error for {} {}: {}", method, path, ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    private String formatMessage(HttpStatus status, String message, String path, String method) {
        return switch (status) {
            case NOT_FOUND -> String.format("No endpoint %s %s.", method, path);
            case METHOD_NOT_ALLOWED -> String.format("Method %s not allowed for %s.", method, path);
            case BAD_REQUEST -> message != null ? message : "Bad request";
            case UNAUTHORIZED -> "Authentication required";
            case FORBIDDEN -> "Access denied";
            case INTERNAL_SERVER_ERROR -> "Internal server error";
            default -> message != null ? message : status.getReasonPhrase();
        };
    }

    private Map<String, Object> createDetails(HttpStatus status, Throwable exception) {
        Map<String, Object> details = Map.of("type", "http_error");
        
        if (exception != null) {
            details = Map.of(
                "type", "http_error",
                "exception_class", exception.getClass().getSimpleName()
            );
        }
        
        return details;
    }

    private String extractTraceId(HttpServletRequest request) {
        String traceId = request.getHeader("X-Trace-ID");
        if (traceId == null) {
            traceId = request.getHeader("X-Request-ID");
        }
        if (traceId == null) {
            traceId = "unknown";
        }
        return traceId;
    }

    public record ErrorResponse(
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