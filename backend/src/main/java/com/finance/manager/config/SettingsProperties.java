package com.finance.manager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.settings")
public class SettingsProperties {

    /**
     * AES key material (hashed to 256 bits); min 32 chars recommended.
     */
    private String encryptionKey = "";

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey != null ? encryptionKey : "";
    }
}
