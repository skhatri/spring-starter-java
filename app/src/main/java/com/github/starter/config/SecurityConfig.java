package com.github.starter.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.WebFilter;
import java.util.List;
import java.util.Map;
import java.util.Set;
@Configuration
@ConditionalOnProperty(name = "security.config.enabled", havingValue = "true", matchIfMissing = true)
public class SecurityConfig {
    @Bean
    @ConfigurationProperties(prefix = "secrets")
    public SecretsConfig secretsConfig() {
        return new SecretsConfig();
    }
    @Bean
    @ConfigurationProperties(prefix = "security-headers")
    public SecurityHeadersConfig securityHeadersConfig() {
        return new SecurityHeadersConfig();
    }
    @Bean
    @ConfigurationProperties(prefix = "cors")
    public CorsConfig corsConfig() {
        return new CorsConfig();
    }
    @Bean
    public WebFilter securityHeadersFilter(SecurityHeadersConfig config) {
        return (exchange, chain) -> {
            if (config.getContentSecurityPolicy() != null) {
                exchange.getResponse().getHeaders().add("Content-Security-Policy", config.getContentSecurityPolicy());
            }
            if (config.getXContentTypeOptions() != null) {
                exchange.getResponse().getHeaders().add("X-Content-Type-Options", config.getXContentTypeOptions());
            }
            if (config.getXFrameOptions() != null) {
                exchange.getResponse().getHeaders().add("X-Frame-Options", config.getXFrameOptions());
            }
            if (config.getXXssProtection() != null) {
                exchange.getResponse().getHeaders().add("X-XSS-Protection", config.getXXssProtection());
            }
            if (config.getStrictTransportSecurity() != null) {
                exchange.getResponse().getHeaders().add("Strict-Transport-Security", config.getStrictTransportSecurity());
            }
            if (config.getReferrerPolicy() != null) {
                exchange.getResponse().getHeaders().add("Referrer-Policy", config.getReferrerPolicy());
            }
            return chain.filter(exchange);
        };
    }
    @Bean
    public CorsWebFilter corsFilter(CorsConfig corsConfig) {
        CorsConfiguration config = new CorsConfiguration();
        if (corsConfig.getAllowedOrigins() != null) {
            config.setAllowedOrigins(corsConfig.getAllowedOrigins());
        }
        if (corsConfig.getAllowedMethods() != null) {
            config.setAllowedMethods(corsConfig.getAllowedMethods());
        }
        if (corsConfig.getAllowedHeaders() != null) {
            config.setAllowedHeaders(corsConfig.getAllowedHeaders());
        }
        if (corsConfig.getExposedHeaders() != null) {
            config.setExposedHeaders(corsConfig.getExposedHeaders());
        }
        config.setAllowCredentials(corsConfig.isAllowCredentials());
        config.setMaxAge(corsConfig.getMaxAge());
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }
    @Bean
    public SecretMaskingFilter secretMaskingFilter(SecretsConfig secretsConfig) {
        return new SecretMaskingFilter(secretsConfig.getMaskedFields());
    }
    public static class SecretsConfig {
        private List<String> mountPaths;
        private List<String> envPrefixes;
        private Set<String> maskedFields;
        public List<String> getMountPaths() {
            return mountPaths;
        }
        public void setMountPaths(List<String> mountPaths) {
            this.mountPaths = mountPaths;
        }
        public List<String> getEnvPrefixes() {
            return envPrefixes;
        }
        public void setEnvPrefixes(List<String> envPrefixes) {
            this.envPrefixes = envPrefixes;
        }
        public Set<String> getMaskedFields() {
            return maskedFields;
        }
        public void setMaskedFields(Set<String> maskedFields) {
            this.maskedFields = maskedFields;
        }
    }
    public static class SecurityHeadersConfig {
        private String contentSecurityPolicy;
        private String xContentTypeOptions;
        private String xFrameOptions;
        private String xXssProtection;
        private String strictTransportSecurity;
        private String referrerPolicy;
        public String getContentSecurityPolicy() {
            return contentSecurityPolicy;
        }
        public void setContentSecurityPolicy(String contentSecurityPolicy) {
            this.contentSecurityPolicy = contentSecurityPolicy;
        }
        public String getXContentTypeOptions() {
            return xContentTypeOptions;
        }
        public void setXContentTypeOptions(String xContentTypeOptions) {
            this.xContentTypeOptions = xContentTypeOptions;
        }
        public String getXFrameOptions() {
            return xFrameOptions;
        }
        public void setXFrameOptions(String xFrameOptions) {
            this.xFrameOptions = xFrameOptions;
        }
        public String getXXssProtection() {
            return xXssProtection;
        }
        public void setXXssProtection(String xXssProtection) {
            this.xXssProtection = xXssProtection;
        }
        public String getStrictTransportSecurity() {
            return strictTransportSecurity;
        }
        public void setStrictTransportSecurity(String strictTransportSecurity) {
            this.strictTransportSecurity = strictTransportSecurity;
        }
        public String getReferrerPolicy() {
            return referrerPolicy;
        }
        public void setReferrerPolicy(String referrerPolicy) {
            this.referrerPolicy = referrerPolicy;
        }
    }
    public static class CorsConfig {
        private List<String> allowedOrigins;
        private List<String> allowedMethods;
        private List<String> allowedHeaders;
        private List<String> exposedHeaders;
        private boolean allowCredentials;
        private long maxAge;
        public List<String> getAllowedOrigins() {
            return allowedOrigins;
        }
        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
        public List<String> getAllowedMethods() {
            return allowedMethods;
        }
        public void setAllowedMethods(List<String> allowedMethods) {
            this.allowedMethods = allowedMethods;
        }
        public List<String> getAllowedHeaders() {
            return allowedHeaders;
        }
        public void setAllowedHeaders(List<String> allowedHeaders) {
            this.allowedHeaders = allowedHeaders;
        }
        public List<String> getExposedHeaders() {
            return exposedHeaders;
        }
        public void setExposedHeaders(List<String> exposedHeaders) {
            this.exposedHeaders = exposedHeaders;
        }
        public boolean isAllowCredentials() {
            return allowCredentials;
        }
        public void setAllowCredentials(boolean allowCredentials) {
            this.allowCredentials = allowCredentials;
        }
        public long getMaxAge() {
            return maxAge;
        }
        public void setMaxAge(long maxAge) {
            this.maxAge = maxAge;
        }
    }
}
