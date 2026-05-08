package com.finance.manager.controller;

import com.finance.manager.model.Transaction;
import com.finance.manager.security.AuthPrincipal;
import com.finance.manager.service.OpenAiCategorySuggestionService;
import com.finance.manager.service.UserOpenAiSettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/ai")
public class AiSuggestionController {

    private final UserOpenAiSettingsService userOpenAiSettingsService;
    private final OpenAiCategorySuggestionService openAiCategorySuggestionService;

    public AiSuggestionController(
            UserOpenAiSettingsService userOpenAiSettingsService,
            OpenAiCategorySuggestionService openAiCategorySuggestionService) {
        this.userOpenAiSettingsService = userOpenAiSettingsService;
        this.openAiCategorySuggestionService = openAiCategorySuggestionService;
    }

    @PostMapping("/suggest-category")
    public ResponseEntity<Map<String, Object>> suggestCategory(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody Map<String, String> body) {

        String description = body != null ? body.get("description") : null;
        Map<String, Object> result = new HashMap<>();

        if (description == null || description.isBlank()) {
            result.put("category", null);
            result.put("source", "invalid");
            return ResponseEntity.ok(result);
        }

        Optional<String> apiKeyOpt = userOpenAiSettingsService.getDecryptedApiKey(principal.userId());
        if (apiKeyOpt.isEmpty()) {
            result.put("category", null);
            result.put("source", "no_api_key");
            return ResponseEntity.ok(result);
        }

        Optional<Transaction.Category> cat =
                openAiCategorySuggestionService.suggestCategory(apiKeyOpt.get(), description);

        if (cat.isPresent()) {
            result.put("category", cat.get().name());
            result.put("source", "openai");
        } else {
            result.put("category", null);
            result.put("source", "openai_failed");
        }
        return ResponseEntity.ok(result);
    }
}
