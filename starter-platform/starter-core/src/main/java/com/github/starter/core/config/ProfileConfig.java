package com.github.starter.core.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
@Configuration
public class ProfileConfig {
    @Profile("dev")
    @Configuration
    public static class DevProfileConfig {
        @Bean
        public ProfileInfo profileInfo(Environment env) {
            return new ProfileInfo("dev", env.getActiveProfiles());
        }
    }
    @Profile("test")
    @Configuration
    public static class TestProfileConfig {
        @Bean
        public ProfileInfo profileInfo(Environment env) {
            return new ProfileInfo("test", env.getActiveProfiles());
        }
    }
    @Profile("prod")
    @Configuration
    public static class ProdProfileConfig {
        @Bean
        public ProfileInfo profileInfo(Environment env) {
            return new ProfileInfo("prod", env.getActiveProfiles());
        }
    }
    public static class ProfileInfo {
        private final String name;
        private final String[] activeProfiles;
        public ProfileInfo(String name, String[] activeProfiles) {
            this.name = name;
            this.activeProfiles = activeProfiles;
        }
        public String getName() {
            return name;
        }
        public String[] getActiveProfiles() {
            return activeProfiles;
        }
    }
}
