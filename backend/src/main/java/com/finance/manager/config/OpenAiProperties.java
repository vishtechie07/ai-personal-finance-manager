package com.finance.manager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.openai")
public class OpenAiProperties {

    private String model = "gpt-4o-mini";
    /** Optional platform key (e.g. OPENAI_API_KEY on Render) used when the user has not saved their own key. */
    private String apiKey = "";

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model != null && !model.isBlank() ? model : "gpt-4o-mini";
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
    }

    public boolean hasPlatformApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }
}
