package com.github.starter.config.duck;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(prefix="core", name = "db", havingValue = "duckdb")
@Configuration
@ConfigurationProperties(prefix = "core.duckdb")
public class DuckParams {
    private String url;
    private DuckInit init;
    private boolean readOnly;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public DuckInit getInit() {
        return init;
    }

    public void setInit(DuckInit init) {
        this.init = init;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
}

class DuckInit {
    private String file;
    private boolean enabled;

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}