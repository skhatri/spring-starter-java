package com.github.starter.core.config;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
@Configuration
@EnableConfigurationProperties({AppConfig.class})
public class BootstrapConfig {
}
