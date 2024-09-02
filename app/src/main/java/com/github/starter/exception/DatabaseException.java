package com.github.starter.exception;

public class DatabaseException extends RuntimeException {
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
