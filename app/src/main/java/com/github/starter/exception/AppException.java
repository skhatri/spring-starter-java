package com.github.starter.exception;

public class AppException extends RuntimeException {
    public AppException(String msg) {
        super(msg);
    }

    public AppException(Throwable cause) {
        super(cause);
    }

    public AppException(String msg, Throwable th) {
        super(msg, th);
    }
}
