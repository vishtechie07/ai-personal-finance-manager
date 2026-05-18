package com.finance.manager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.seed")
public class SeedProperties {

    /**
     * When true, on startup if the seed user is missing but other users exist,
     * all users/transactions/budgets are wiped and the seed account is created.
     * Credentials: {@link com.finance.manager.service.DemoSeedService} / docs/DEMO_CREDENTIALS.md.
     * Enable for local/dev only (see application-local.yml).
     */
    private boolean consolidateLegacyUsers = false;

    public boolean isConsolidateLegacyUsers() {
        return consolidateLegacyUsers;
    }

    public void setConsolidateLegacyUsers(boolean consolidateLegacyUsers) {
        this.consolidateLegacyUsers = consolidateLegacyUsers;
    }
}
