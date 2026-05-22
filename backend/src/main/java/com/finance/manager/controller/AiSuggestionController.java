package com.finance.manager.controller;

import com.finance.manager.model.Transaction;
import com.finance.manager.security.AuthPrincipal;
import com.finance.manager.service.AiGateService;
import com.finance.manager.service.OpenAiCategorySuggestionService;
import com.finance.manager.service.OpenAiKeyResolver;
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

    private final AiGateService aiGateService;
    private final OpenAiCategorySuggestionService openAiCategorySuggestionService;

    public AiSuggestionController(
            AiGateService aiGateService,
            OpenAiCategorySuggestionService openAiCategorySuggestionService) {
        this.aiGateService = aiGateService;
        this.openAiCategorySuggestionService = openAiCategorySuggestionService;
    }

    @PostMapping("/suggest-category")
    public ResponseEntity<Map<String, Object>> suggestCategory(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody Map<String, String> body) {

        String description = body != null ? body.get("description") : null;
        Map<String, Object> result = new HashMap<>();

        Optional<String> normalized = aiGateService.truncateDescription(description);
        if (normalized.isEmpty()) {
            result.put("category", null);
            result.put("source", "invalid");
            return ResponseEntity.ok(result);
        }

        AiGateService.AccessResult access = aiGateService.evaluate(
                principal.userId(), principal.username(), AiGateService.Operation.CATEGORY, description);

        if (access instanceof AiGateService.AccessResult.Denied denied) {
            result.put("category", null);
            result.put("source", denied.source());
            return ResponseEntity.ok(result);
        }

        OpenAiKeyResolver.ResolvedKey key =
                ((AiGateService.AccessResult.Allowed) access).key();
        Optional<Transaction.Category> cat =
                openAiCategorySuggestionService.suggestCategory(key.apiKey(), normalized.get());

        if (cat.isPresent()) {
            result.put("category", cat.get().name());
            result.put("source", key.source() == OpenAiKeyResolver.ResolvedKey.Source.PLATFORM
                    ? "openai_platform"
                    : "openai");
        } else {
            result.put("category", null);
            result.put("source", "openai_failed");
        }
        return ResponseEntity.ok(result);
    }
}
