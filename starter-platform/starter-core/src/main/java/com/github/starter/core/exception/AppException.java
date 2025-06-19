package com.github.starter.core.exception;

import java.util.Map;

public class AppException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    private final String errorCode;
    private final Map<String, Object> context;
    
    public AppException(String msg) {
        super(msg);
        this.errorCode = "APP_ERROR";
        this.context = Map.of();
    }
    
    public AppException(Throwable cause) {
        super(cause);
        this.errorCode = "APP_ERROR";
        this.context = Map.of();
    }
    
    public AppException(String msg, Throwable th) {
        super(msg, th);
        this.errorCode = "APP_ERROR";
        this.context = Map.of();
    }
    
    public AppException(String errorCode, String msg, Map<String, Object> context) {
        super(msg);
        this.errorCode = errorCode;
        this.context = context != null ? Map.copyOf(context) : Map.of();
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public Map<String, Object> getContext() {
        return context;
    }
}
