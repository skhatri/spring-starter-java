package com.github.starter.core.exception;
public class DatabaseException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public DatabaseException(String msg) {
        super(msg);
    }
    public DatabaseException(Throwable cause) {
        super(cause);
    }
    public DatabaseException(String msg, Throwable th) {
        super(msg, th);
    }
}
