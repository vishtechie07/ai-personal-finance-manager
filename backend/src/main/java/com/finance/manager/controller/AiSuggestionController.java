package com.finance.manager.controller;

import com.finance.manager.model.Transaction;
import com.finance.manager.security.AuthPrincipal;
import com.finance.manager.service.AiGateService;
import com.finance.manager.service.FinancialMonthSummaryService;
import com.finance.manager.service.OpenAiCategorySuggestionService;
import com.finance.manager.service.OpenAiKeyResolver;
import com.finance.manager.service.OpenAiMonthlyBriefService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/ai")
public class AiSuggestionController {

    private final AiGateService aiGateService;
    private final OpenAiCategorySuggestionService openAiCategorySuggestionService;
    private final FinancialMonthSummaryService financialMonthSummaryService;
    private final OpenAiMonthlyBriefService openAiMonthlyBriefService;

    public AiSuggestionController(
            AiGateService aiGateService,
            OpenAiCategorySuggestionService openAiCategorySuggestionService,
            FinancialMonthSummaryService financialMonthSummaryService,
            OpenAiMonthlyBriefService openAiMonthlyBriefService) {
        this.aiGateService = aiGateService;
        this.openAiCategorySuggestionService = openAiCategorySuggestionService;
        this.financialMonthSummaryService = financialMonthSummaryService;
        this.openAiMonthlyBriefService = openAiMonthlyBriefService;
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

    @PostMapping("/monthly-brief")
    public ResponseEntity<Map<String, Object>> monthlyBrief(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody Map<String, String> body) {

        Map<String, Object> result = new HashMap<>();
        String rawMonth = body != null ? body.get("yearMonth") : null;
        YearMonth month;
        try {
            month = rawMonth != null && !rawMonth.isBlank()
                    ? YearMonth.parse(rawMonth.trim())
                    : YearMonth.now();
        } catch (Exception e) {
            result.put("source", "invalid");
            result.put("bullets", List.of());
            result.put("recommendation", "");
            return ResponseEntity.badRequest().body(result);
        }

        result.put("yearMonth", month.toString());
        String summaryJson = financialMonthSummaryService.buildSummaryJson(principal.userId(), month);
        result.put("summary", summaryJson);

        AiGateService.AccessResult access = aiGateService.evaluate(
                principal.userId(), principal.username(), AiGateService.Operation.MONTHLY_BRIEF, null);
        if (access instanceof AiGateService.AccessResult.Denied denied) {
            result.put("source", denied.source());
            result.put("bullets", fallbackBullets(summaryJson));
            result.put("recommendation", fallbackRecommendation(summaryJson));
            return ResponseEntity.ok(result);
        }

        OpenAiKeyResolver.ResolvedKey key =
                ((AiGateService.AccessResult.Allowed) access).key();
        Optional<OpenAiMonthlyBriefService.BriefResult> brief =
                openAiMonthlyBriefService.generateBrief(key.apiKey(), summaryJson);

        if (brief.isPresent()) {
            result.put("bullets", brief.get().bullets());
            result.put("recommendation", brief.get().recommendation());
            result.put("source", key.source() == OpenAiKeyResolver.ResolvedKey.Source.PLATFORM
                    ? "openai_platform"
                    : "openai");
        } else {
            result.put("source", "openai_failed");
            result.put("bullets", fallbackBullets(summaryJson));
            result.put("recommendation", fallbackRecommendation(summaryJson));
        }
        return ResponseEntity.ok(result);
    }

    private List<String> fallbackBullets(String summaryJson) {
        List<String> bullets = new ArrayList<>();
        bullets.add("AI brief unavailable — review your dashboard totals for this month.");
        if (summaryJson != null && summaryJson.contains("totalExpenses")) {
            bullets.add("Expense and budget figures are still available in the summary data below.");
        }
        return bullets;
    }

    private String fallbackRecommendation(String summaryJson) {
        return "Add your OpenAI key in Settings or enable SpendSense AI to generate a personalized narrative.";
    }
}
