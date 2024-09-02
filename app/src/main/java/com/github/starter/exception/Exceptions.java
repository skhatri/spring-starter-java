package com.github.starter.exception;

public class Exceptions {
    private Exceptions() {
    }

    public static AppException appException(Throwable th) {
        return new AppException(th);
    }

    public static AppException appException(String msg, Throwable th) {
        return new AppException(msg, th);
    }

    public static DatabaseException dbException(Throwable th) {
        return new DatabaseException(th);
    }

    public static DatabaseException dbException(String msg, Throwable th) {
        return new DatabaseException(msg, th);
    }
}
