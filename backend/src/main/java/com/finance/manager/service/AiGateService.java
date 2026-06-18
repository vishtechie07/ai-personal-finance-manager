package com.finance.manager.service;

import com.finance.manager.config.AiProperties;
import com.finance.manager.model.User;
import com.finance.manager.repository.UserRepository;
import com.finance.manager.service.OpenAiKeyResolver.ResolvedKey;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Per-user AI quotas and stricter rules when using the platform OpenAI key.
 */
@Service
public class AiGateService {

    private static final DateTimeFormatter ISO_LOCAL = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public enum Operation {
        CATEGORY,
        RECEIPT,
        MONTHLY_BRIEF
    }

    public sealed interface AccessResult permits AccessResult.Allowed, AccessResult.Denied {

        record Allowed(ResolvedKey key) implements AccessResult {}

        record Denied(String source) implements AccessResult {}
    }

    public record PlatformTrialStatus(
            int trialMinutes,
            boolean trialConfigured,
            boolean trialActive,
            boolean trialExpired,
            String trialEndsAt) {}

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

    public PlatformTrialStatus getPlatformTrialStatus(Long userId, boolean hasUserApiKey) {
        int trialMinutes = aiProperties.getPlatformTrialMinutes();
        boolean trialConfigured = trialMinutes > 0 && isPlatformAiConfigured();
        if (!trialConfigured || hasUserApiKey) {
            return new PlatformTrialStatus(trialMinutes, trialConfigured, false, false, null);
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getCreatedAt() == null) {
            return new PlatformTrialStatus(trialMinutes, trialConfigured, false, false, null);
        }
        LocalDateTime trialEnd = user.getCreatedAt().plusMinutes(trialMinutes);
        LocalDateTime now = LocalDateTime.now();
        boolean active = !now.isAfter(trialEnd);
        boolean expired = now.isAfter(trialEnd);
        String endsAt = trialEnd.format(ISO_LOCAL);
        return new PlatformTrialStatus(trialMinutes, trialConfigured, active, expired, endsAt);
    }

    public boolean isAiAvailableForUser(Long userId, boolean hasUserApiKey) {
        if (hasUserApiKey) {
            return true;
        }
        if (!isPlatformAiConfigured()) {
            return false;
        }
        PlatformTrialStatus trial = getPlatformTrialStatus(userId, false);
        if (trial.trialConfigured()) {
            return trial.trialActive();
        }
        return true;
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
        int trialMinutes = aiProperties.getPlatformTrialMinutes();
        if (trialMinutes > 0) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null && user.getCreatedAt() != null) {
                LocalDateTime trialEnd = user.getCreatedAt().plusMinutes(trialMinutes);
                if (LocalDateTime.now().isAfter(trialEnd)) {
                    return Optional.of("platform_trial_expired");
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
            return switch (operation) {
                case CATEGORY -> aiProperties.getPlatformCategoryQuotaPerWindow();
                case RECEIPT -> aiProperties.getPlatformReceiptQuotaPerWindow();
                case MONTHLY_BRIEF -> aiProperties.getPlatformMonthlyBriefQuotaPerWindow();
            };
        }
        return switch (operation) {
            case CATEGORY -> aiProperties.getUserCategoryQuotaPerWindow();
            case RECEIPT -> aiProperties.getUserReceiptQuotaPerWindow();
            case MONTHLY_BRIEF -> aiProperties.getUserMonthlyBriefQuotaPerWindow();
        };
    }
}
