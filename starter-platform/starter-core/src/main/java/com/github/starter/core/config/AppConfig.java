package com.github.starter.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "core")
public record AppConfig(
    String db,
    String environment,
    String version
) {}
