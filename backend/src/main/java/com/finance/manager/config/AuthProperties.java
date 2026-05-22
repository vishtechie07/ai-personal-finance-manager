package com.finance.manager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

    /** Allow POST /auth/register when false (Google sign-in may still work). */
    private boolean registrationEnabled = true;

    /** Max requests per client IP per window for sensitive auth/AI routes. */
    private int rateLimitPerWindow = 30;

    /** Rate limit window length in seconds. */
    private int rateLimitWindowSeconds = 60;

    public boolean isRegistrationEnabled() {
        return registrationEnabled;
    }

    public void setRegistrationEnabled(boolean registrationEnabled) {
        this.registrationEnabled = registrationEnabled;
    }

    public int getRateLimitPerWindow() {
        return rateLimitPerWindow;
    }

    public void setRateLimitPerWindow(int rateLimitPerWindow) {
        this.rateLimitPerWindow = rateLimitPerWindow;
    }

    public int getRateLimitWindowSeconds() {
        return rateLimitWindowSeconds;
    }

    public void setRateLimitWindowSeconds(int rateLimitWindowSeconds) {
        this.rateLimitWindowSeconds = rateLimitWindowSeconds;
    }
}
