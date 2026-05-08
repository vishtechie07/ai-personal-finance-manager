package com.finance.manager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.openai")
public class OpenAiProperties {

    private String model = "gpt-4o-mini";

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model != null && !model.isBlank() ? model : "gpt-4o-mini";
    }
}
