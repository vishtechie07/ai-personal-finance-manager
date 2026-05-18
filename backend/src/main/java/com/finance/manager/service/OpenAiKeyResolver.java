package com.finance.manager.service;

import com.finance.manager.config.OpenAiProperties;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Resolves which OpenAI API key to use: per-user key first, then optional platform key from env.
 */
@Service
public class OpenAiKeyResolver {

    public record ResolvedKey(String apiKey, Source source) {
        public enum Source {
            USER,
            PLATFORM
        }
    }

    private final UserOpenAiSettingsService userOpenAiSettingsService;
    private final OpenAiProperties openAiProperties;

    public OpenAiKeyResolver(
            UserOpenAiSettingsService userOpenAiSettingsService,
            OpenAiProperties openAiProperties) {
        this.userOpenAiSettingsService = userOpenAiSettingsService;
        this.openAiProperties = openAiProperties;
    }

    public boolean isPlatformKeyConfigured() {
        return openAiProperties.hasPlatformApiKey();
    }

    public Optional<ResolvedKey> resolveForUser(Long userId) {
        Optional<String> userKey = userOpenAiSettingsService.getDecryptedApiKey(userId);
        if (userKey.isPresent()) {
            return Optional.of(new ResolvedKey(userKey.get(), ResolvedKey.Source.USER));
        }
        if (openAiProperties.hasPlatformApiKey()) {
            return Optional.of(
                    new ResolvedKey(openAiProperties.getApiKey(), ResolvedKey.Source.PLATFORM));
        }
        return Optional.empty();
    }
}
