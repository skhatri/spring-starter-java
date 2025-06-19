package com.github.starter.core.logging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
public final class LoggingUtils {
    private LoggingUtils() {
    }
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
    public static String addTraceId() {
        String traceId = MDC.get("traceId");
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
            MDC.put("traceId", traceId);
        }
        return traceId;
    }
    public static void addContext(String key, String value) {
        MDC.put(key, value);
    }
    public static void addContext(Map<String, String> context) {
        context.forEach(MDC::put);
    }
    public static void removeContext(String key) {
        MDC.remove(key);
    }
    public static void clearContext() {
        MDC.clear();
    }
    public static <T> T withContext(Map<String, String> context, Supplier<T> function) {
        try {
            addContext(context);
            return function.get();
        } finally {
            clearContext();
        }
    }
}
