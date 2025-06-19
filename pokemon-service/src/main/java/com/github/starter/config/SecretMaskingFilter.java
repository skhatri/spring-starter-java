package com.github.starter.config;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecretMaskingFilter implements WebFilter {

    private static final String MASK_VALUE = "********";
    private final Set<String> maskedFields;
    private final Pattern pattern;

    public SecretMaskingFilter(Set<String> maskedFields) {
        this.maskedFields = maskedFields != null ? maskedFields : Collections.emptySet();
        
        if (this.maskedFields.isEmpty()) {
            this.pattern = Pattern.compile("$^");
        } else {
            String fieldsRegex = String.join("|", 
                this.maskedFields.stream()
                    .map(Pattern::quote)
                    .toArray(String[]::new)
            );
            String regex = "(" + fieldsRegex + ")(=([^&]*)|(?=&|$))";
            this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        }
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerWebExchange maskedExchange = maskSensitiveData(exchange);
        return chain.filter(maskedExchange);
    }

    private ServerWebExchange maskSensitiveData(ServerWebExchange exchange) {
        String query = exchange.getRequest().getURI().getQuery();

        if (query != null && !query.isEmpty() && !maskedFields.isEmpty()) {
            String maskedQuery = pattern.matcher(query).replaceAll(matchResult -> {
                String fieldName = matchResult.group(1);
                return fieldName + "=" + MASK_VALUE;
            });
            
            if (!query.equals(maskedQuery)) {
                exchange.getAttributes().put("maskedQuery", maskedQuery);
            }
        }

        return exchange;
    }
} 