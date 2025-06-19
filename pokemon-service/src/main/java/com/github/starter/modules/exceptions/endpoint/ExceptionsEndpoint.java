package com.github.starter.modules.exceptions.endpoint;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.starter.core.exception.AppException;
import com.github.starter.core.exception.DatabaseException;

@RestController
public class ExceptionsEndpoint {

    @GetMapping("/exceptions/400")
    public ResponseEntity<String> trigger400Error(@RequestParam(defaultValue = "Bad Request Error") String message) {
        throw new AppException("BAD_REQUEST", message, Map.of("errorCode", 400));
    }

    @GetMapping("/exceptions/404")
    public ResponseEntity<String> trigger404Error(@RequestParam(defaultValue = "Resource Not Found") String message) {
        throw new IllegalArgumentException(message);
    }

    @GetMapping("/exceptions/500")
    public ResponseEntity<String> trigger500Error(@RequestParam(defaultValue = "Internal Server Error") String message) {
        throw new DatabaseException(message, new RuntimeException("Database connection failed"));
    }

    @GetMapping("/exceptions/runtime-error")
    public ResponseEntity<String> triggerRuntimeError(@RequestParam(defaultValue = "Runtime Error") String message) {
        throw new RuntimeException(message);
    }


    @GetMapping("/exceptions/custom/{status}")
    public ResponseEntity<String> triggerCustomStatusError(
            @PathVariable int status,
            @RequestParam(defaultValue = "Custom error") String message) {
        
        if (status == 400) {
            throw new IllegalArgumentException(message + " (Custom 400)");
        } else if (status == 404) {
            throw new IllegalArgumentException(message + " (Custom 404)");
        } else if (status == 500) {
            throw new RuntimeException(message + " (Custom 500)");
        }
        
        HttpStatus httpStatus = HttpStatus.resolve(status);
        if (httpStatus == null) {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        
        return ResponseEntity.status(httpStatus).body("Custom status: " + status);
    }

    @GetMapping("/exceptions/success")
    public ResponseEntity<Map<String, Object>> success() {
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "All systems operational",
            "timestamp", System.currentTimeMillis(),
            "endpoint", "/exceptions/success"
        ));
    }
} 