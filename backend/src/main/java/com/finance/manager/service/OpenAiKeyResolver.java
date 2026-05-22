package com.finance.manager.service;

import com.finance.manager.config.AiProperties;
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
    private final AiProperties aiProperties;

    public OpenAiKeyResolver(
            UserOpenAiSettingsService userOpenAiSettingsService,
            OpenAiProperties openAiProperties,
            AiProperties aiProperties) {
        this.userOpenAiSettingsService = userOpenAiSettingsService;
        this.openAiProperties = openAiProperties;
        this.aiProperties = aiProperties;
    }

    public boolean isPlatformKeyConfigured() {
        return openAiProperties.hasPlatformApiKey() && aiProperties.isPlatformEnabled();
    }

    public Optional<ResolvedKey> resolveForUser(Long userId) {
        Optional<String> userKey = userOpenAiSettingsService.getDecryptedApiKey(userId);
        if (userKey.isPresent()) {
            return Optional.of(new ResolvedKey(userKey.get(), ResolvedKey.Source.USER));
        }
        if (openAiProperties.hasPlatformApiKey() && aiProperties.isPlatformEnabled()) {
            return Optional.of(
                    new ResolvedKey(openAiProperties.getApiKey(), ResolvedKey.Source.PLATFORM));
        }
        return Optional.empty();
    }
}
