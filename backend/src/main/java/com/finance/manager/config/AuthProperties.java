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

    /** Stricter limit for POST /auth/register (per IP). */
    private int registerAttemptsPerWindow = 5;

    private int registerAttemptWindowSeconds = 3600;

    /** Max new accounts (register + Google) per IP per 24h; 0 = unlimited. */
    private int maxNewAccountsPerIpPerDay = 5;

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

    public int getRegisterAttemptsPerWindow() {
        return registerAttemptsPerWindow;
    }

    public void setRegisterAttemptsPerWindow(int registerAttemptsPerWindow) {
        this.registerAttemptsPerWindow = Math.max(1, registerAttemptsPerWindow);
    }

    public int getRegisterAttemptWindowSeconds() {
        return registerAttemptWindowSeconds;
    }

    public void setRegisterAttemptWindowSeconds(int registerAttemptWindowSeconds) {
        this.registerAttemptWindowSeconds = Math.max(60, registerAttemptWindowSeconds);
    }

    public int getMaxNewAccountsPerIpPerDay() {
        return maxNewAccountsPerIpPerDay;
    }

    public void setMaxNewAccountsPerIpPerDay(int maxNewAccountsPerIpPerDay) {
        this.maxNewAccountsPerIpPerDay = Math.max(0, maxNewAccountsPerIpPerDay);
    }
}
