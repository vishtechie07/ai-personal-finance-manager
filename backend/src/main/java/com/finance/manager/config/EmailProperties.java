package com.finance.manager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.email")
public class EmailProperties {

    private boolean enabled = false;
    private String resendApiKey = "";
    private String fromAddress = "SpendSense <noreply@spendsense.app>";
    private int maxRemindersPerUserPerDay = 3;

    public boolean isEnabled() {
        return enabled && resendApiKey != null && !resendApiKey.isBlank();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getResendApiKey() {
        return resendApiKey;
    }

    public void setResendApiKey(String resendApiKey) {
        this.resendApiKey = resendApiKey != null ? resendApiKey.trim() : "";
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public int getMaxRemindersPerUserPerDay() {
        return maxRemindersPerUserPerDay;
    }

    public void setMaxRemindersPerUserPerDay(int maxRemindersPerUserPerDay) {
        this.maxRemindersPerUserPerDay = Math.max(1, maxRemindersPerUserPerDay);
    }
}
