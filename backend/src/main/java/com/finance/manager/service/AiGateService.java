package com.finance.manager.service;

import com.finance.manager.config.AiProperties;
import com.finance.manager.model.User;
import com.finance.manager.repository.UserRepository;
import com.finance.manager.service.OpenAiKeyResolver.ResolvedKey;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Per-user AI quotas and stricter rules when using the platform OpenAI key.
 */
@Service
public class AiGateService {

    public enum Operation {
        CATEGORY,
        RECEIPT
    }

    public sealed interface AccessResult permits AccessResult.Allowed, AccessResult.Denied {

        record Allowed(ResolvedKey key) implements AccessResult {}

        record Denied(String source) implements AccessResult {}
    }

    private final AiProperties aiProperties;
    private final OpenAiKeyResolver openAiKeyResolver;
    private final UserRepository userRepository;
    private final SlidingWindowRateLimiter rateLimiter;

    public AiGateService(
            AiProperties aiProperties,
            OpenAiKeyResolver openAiKeyResolver,
            UserRepository userRepository,
            SlidingWindowRateLimiter rateLimiter) {
        this.aiProperties = aiProperties;
        this.openAiKeyResolver = openAiKeyResolver;
        this.userRepository = userRepository;
        this.rateLimiter = rateLimiter;
    }

    public boolean isPlatformAiConfigured() {
        return openAiKeyResolver.isPlatformKeyConfigured() && aiProperties.isPlatformEnabled();
    }

    public Optional<String> truncateDescription(String description) {
        if (description == null) {
            return Optional.empty();
        }
        String trimmed = description.trim();
        if (trimmed.isEmpty()) {
            return Optional.empty();
        }
        int max = aiProperties.getMaxDescriptionLength();
        if (trimmed.length() <= max) {
            return Optional.of(trimmed);
        }
        return Optional.of(trimmed.substring(0, max));
    }

    public AccessResult evaluate(Long userId, String username, Operation operation, String description) {
        if (operation == Operation.CATEGORY) {
            if (truncateDescription(description).isEmpty()) {
                return new AccessResult.Denied("invalid");
            }
        }

        Optional<ResolvedKey> resolved = openAiKeyResolver.resolveForUser(userId);
        if (resolved.isEmpty()) {
            return new AccessResult.Denied("no_api_key");
        }

        ResolvedKey key = resolved.get();
        if (key.source() == ResolvedKey.Source.PLATFORM) {
            Optional<String> platformDenial = checkPlatformRules(userId, username);
            if (platformDenial.isPresent()) {
                return new AccessResult.Denied(platformDenial.get());
            }
        }

        boolean platform = key.source() == ResolvedKey.Source.PLATFORM;
        if (!rateLimiter.tryConsume(
                quotaKey(userId, operation, platform),
                quotaLimit(operation, platform),
                aiProperties.getQuotaWindowSeconds() * 1000L)) {
            return new AccessResult.Denied("rate_limited");
        }

        return new AccessResult.Allowed(key);
    }

    private Optional<String> checkPlatformRules(Long userId, String username) {
        if (!aiProperties.isPlatformEnabled()) {
            return Optional.of("platform_disabled");
        }
        if (!openAiKeyResolver.isPlatformKeyConfigured()) {
            return Optional.of("no_api_key");
        }
        if (aiProperties.isBlockDemoUserPlatformAi()
                && username != null
                && DemoSeedService.DEMO_USERNAME.equalsIgnoreCase(username)) {
            return Optional.of("demo_not_allowed");
        }
        int minHours = aiProperties.getPlatformMinAccountAgeHours();
        if (minHours > 0) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null && user.getCreatedAt() != null) {
                LocalDateTime eligible = user.getCreatedAt().plusHours(minHours);
                if (LocalDateTime.now().isBefore(eligible)) {
                    return Optional.of("account_too_new");
                }
            }
        }
        return Optional.empty();
    }

    private String quotaKey(Long userId, Operation operation, boolean platform) {
        return "ai:" + userId + ":" + operation.name() + ":" + (platform ? "platform" : "user");
    }

    private int quotaLimit(Operation operation, boolean platform) {
        if (platform) {
            return operation == Operation.CATEGORY
                    ? aiProperties.getPlatformCategoryQuotaPerWindow()
                    : aiProperties.getPlatformReceiptQuotaPerWindow();
        }
        return operation == Operation.CATEGORY
                ? aiProperties.getUserCategoryQuotaPerWindow()
                : aiProperties.getUserReceiptQuotaPerWindow();
    }
}
