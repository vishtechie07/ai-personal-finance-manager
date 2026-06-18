package com.finance.manager.controller;

import com.finance.manager.repository.UserRepository;
import com.finance.manager.security.AuthPrincipal;
import com.finance.manager.service.AiGateService;
import com.finance.manager.service.UserAccountService;
import com.finance.manager.service.UserOpenAiSettingsService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/settings")
public class SettingsController {

    private final UserOpenAiSettingsService userOpenAiSettingsService;
    private final AiGateService aiGateService;
    private final UserAccountService userAccountService;
    private final UserRepository userRepository;

    public SettingsController(
            UserOpenAiSettingsService userOpenAiSettingsService,
            AiGateService aiGateService,
            UserAccountService userAccountService,
            UserRepository userRepository) {
        this.userOpenAiSettingsService = userOpenAiSettingsService;
        this.aiGateService = aiGateService;
        this.userAccountService = userAccountService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getSettings(@AuthenticationPrincipal AuthPrincipal principal) {
        boolean hasKey = userOpenAiSettingsService.hasApiKey(principal.userId());
        AiGateService.PlatformTrialStatus trial =
                aiGateService.getPlatformTrialStatus(principal.userId(), hasKey);

        Map<String, Object> body = new HashMap<>();
        body.put("hasOpenAiApiKey", hasKey);
        body.put("platformAiEnabled", aiGateService.isPlatformAiConfigured());
        body.put("aiAvailable", aiGateService.isAiAvailableForUser(principal.userId(), hasKey));
        body.put("platformTrialMinutes", trial.trialMinutes());
        body.put("platformTrialConfigured", trial.trialConfigured());
        body.put("platformTrialActive", trial.trialActive());
        body.put("platformTrialExpired", trial.trialExpired());
        body.put("platformTrialEndsAt", trial.trialEndsAt());
        userRepository.findById(principal.userId())
                .ifPresent(user -> body.put("profile", userAccountService.toUserInfo(user)));
        return ResponseEntity.ok(body);
    }

    @PutMapping("/preferences")
    public ResponseEntity<Map<String, Object>> updatePreferences(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody PreferencesRequest request) {
        userAccountService.updatePreferences(
                principal.userId(), request.billRemindersEnabled(), request.billReminderDaysBefore());
        return userRepository.findById(principal.userId())
                .map(user -> ResponseEntity.ok(userAccountService.toUserInfo(user)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/openai-api-key")
    public ResponseEntity<Map<String, Object>> saveOpenAiApiKey(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody @jakarta.validation.Valid OpenAiKeyRequest request) {
        userOpenAiSettingsService.saveApiKey(principal.userId(), request.apiKey());
        return ResponseEntity.ok(Map.of("hasOpenAiApiKey", true));
    }

    @DeleteMapping("/openai-api-key")
    public ResponseEntity<Map<String, Object>> deleteOpenAiApiKey(@AuthenticationPrincipal AuthPrincipal principal) {
        userOpenAiSettingsService.clearApiKey(principal.userId());
        return ResponseEntity.ok(Map.of("hasOpenAiApiKey", false));
    }

    public record OpenAiKeyRequest(
            @NotBlank(message = "API key is required")
            @Size(min = 20, max = 2048)
            @Pattern(regexp = "^sk-[a-zA-Z0-9._-]+$", message = "Must be a valid OpenAI secret key (starts with sk-)")
            String apiKey
    ) {}

    public record PreferencesRequest(Boolean billRemindersEnabled, Integer billReminderDaysBefore) {}
}
