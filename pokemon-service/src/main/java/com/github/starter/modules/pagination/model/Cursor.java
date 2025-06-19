package com.github.starter.modules.pagination.model;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public record Cursor(
    Object lastValue,
    Long lastId,
    SortDirection direction
) {
    private static final Logger logger = LoggerFactory.getLogger(Cursor.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public Cursor {
        if (lastId == null || lastId <= 0) {
            throw new IllegalArgumentException("Last ID must be positive");
        }
        if (direction == null) {
            throw new IllegalArgumentException("Direction cannot be null");
        }
    }
    
    public String encode() {
        try {
            CursorData data = new CursorData(lastValue, lastId, direction);
            String json = objectMapper.writeValueAsString(data);
            return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException e) {
            logger.error("Failed to encode cursor: {}", e.getMessage());
            throw new CursorEncodingException("Failed to encode cursor", e);
        }
    }
    
    public static Optional<Cursor> decode(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return Optional.empty();
        }
        
        try {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(encoded);
            String json = new String(decodedBytes, StandardCharsets.UTF_8);
            CursorData data = objectMapper.readValue(json, CursorData.class);
            
            return Optional.of(new Cursor(data.lastValue(), data.lastId(), data.direction()));
        } catch (JsonProcessingException e) {
            logger.warn("Failed to decode cursor '{}': {}", encoded, e.getMessage());
            return Optional.empty();
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to decode cursor '{}': {}", encoded, e.getMessage());
            return Optional.empty();
        }
    }
    
    public static Cursor create(Object lastValue, Long lastId, SortDirection direction) {
        return new Cursor(lastValue, lastId, direction);
    }
    
    public Cursor withDirection(SortDirection newDirection) {
        return new Cursor(lastValue, lastId, newDirection);
    }
    
    private record CursorData(Object lastValue, Long lastId, SortDirection direction) {}
    
    public static class CursorEncodingException extends RuntimeException {
        public CursorEncodingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
} 