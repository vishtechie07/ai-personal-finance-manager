package com.finance.manager.service;

import com.finance.manager.config.AuthProperties;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Signup abuse controls: reserved names, disposable email domains, per-IP creation limits.
 */
@Service
public class RegistrationAbuseService {

    private static final long DAY_MS = 86_400_000L;

    private static final Set<String> RESERVED_USERNAMES = Set.of(
            "spendsense",
            "admin",
            "administrator",
            "root",
            "system",
            "support",
            "demo");

    private static final Set<String> DISPOSABLE_EMAIL_DOMAINS = Set.of(
            "mailinator.com",
            "guerrillamail.com",
            "guerrillamail.net",
            "tempmail.com",
            "temp-mail.org",
            "yopmail.com",
            "10minutemail.com",
            "trashmail.com",
            "getnada.com",
            "sharklasers.com",
            "dispostable.com",
            "fakeinbox.com",
            "maildrop.cc");

    private final AuthProperties authProperties;
    private final SlidingWindowRateLimiter rateLimiter;

    public RegistrationAbuseService(AuthProperties authProperties, SlidingWindowRateLimiter rateLimiter) {
        this.authProperties = authProperties;
        this.rateLimiter = rateLimiter;
    }

    public Optional<String> validateRegistration(String username, String email) {
        Optional<String> common = validateCommon(username, email);
        if (common.isPresent()) {
            return common;
        }
        return Optional.empty();
    }

    public Optional<String> validateNewAccountFromIp(String clientIp) {
        if (clientIp == null || clientIp.isBlank()) {
            return Optional.empty();
        }
        int max = authProperties.getMaxNewAccountsPerIpPerDay();
        if (max <= 0) {
            return Optional.empty();
        }
        String key = "newacct:" + clientIp.trim();
        if (!rateLimiter.tryConsume(key, max, DAY_MS)) {
            return Optional.of("Too many new accounts from this network. Try again tomorrow.");
        }
        return Optional.empty();
    }

    private Optional<String> validateCommon(String username, String email) {
        if (username != null && RESERVED_USERNAMES.contains(username.toLowerCase(Locale.ROOT))) {
            return Optional.of("This username is reserved.");
        }
        if (email != null && isDisposableEmail(email)) {
            return Optional.of("Disposable email addresses are not allowed. Use a permanent email.");
        }
        return Optional.empty();
    }

    private boolean isDisposableEmail(String email) {
        String lower = email.toLowerCase(Locale.ROOT).trim();
        int at = lower.lastIndexOf('@');
        if (at < 0 || at == lower.length() - 1) {
            return false;
        }
        String domain = lower.substring(at + 1);
        return DISPOSABLE_EMAIL_DOMAINS.contains(domain);
    }
}
