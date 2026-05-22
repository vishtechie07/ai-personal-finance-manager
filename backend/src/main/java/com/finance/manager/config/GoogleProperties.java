package com.finance.manager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.google")
public class GoogleProperties {

    private String clientId = "";

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId != null ? clientId.trim() : "";
    }

    public boolean isEnabled() {
        return clientId != null && !clientId.isBlank();
    }
}
