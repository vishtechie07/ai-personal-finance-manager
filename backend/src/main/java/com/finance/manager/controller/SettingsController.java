package com.finance.manager.controller;

import com.finance.manager.security.AuthPrincipal;
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

    public SettingsController(UserOpenAiSettingsService userOpenAiSettingsService) {
        this.userOpenAiSettingsService = userOpenAiSettingsService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getSettings(@AuthenticationPrincipal AuthPrincipal principal) {
        Map<String, Object> body = new HashMap<>();
        body.put("hasOpenAiApiKey", userOpenAiSettingsService.hasApiKey(principal.userId()));
        return ResponseEntity.ok(body);
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
}
