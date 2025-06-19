package com.github.starter.core.logging;
import org.slf4j.Logger;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestTracingFilter implements WebFilter {
    private static final Logger log = LoggingUtils.getLogger(RequestTracingFilter.class);
    private static final String TRACE_ID_HEADER = "X-Trace-ID";
    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {
        String traceId = serverWebExchange.getRequest().getId();
        if (traceId == null || traceId.isBlank()) {
            traceId = LoggingUtils.addTraceId();
            log.debug("Generated new trace ID: {}", traceId);
        } else {
            LoggingUtils.addContext("traceId", traceId);
            log.debug("Using existing trace ID: {}", traceId);
        }
        serverWebExchange.getResponse().getHeaders().add(TRACE_ID_HEADER, traceId);
        LoggingUtils.addContext("path", serverWebExchange.getRequest().getPath().value());
        LoggingUtils.addContext("method", serverWebExchange.getRequest().getMethod().name());
        return webFilterChain.filter(serverWebExchange)
                .doFinally(signalType -> LoggingUtils.clearContext());
    }
}
