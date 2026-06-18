package com.finance.manager.controller;

import com.finance.manager.model.CategoryRule;
import com.finance.manager.security.AuthPrincipal;
import com.finance.manager.service.CategoryRuleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category-rules")
public class CategoryRuleController {

    private final CategoryRuleService categoryRuleService;

    public CategoryRuleController(CategoryRuleService categoryRuleService) {
        this.categoryRuleService = categoryRuleService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryRule>> list(@AuthenticationPrincipal AuthPrincipal principal) {
        return ResponseEntity.ok(categoryRuleService.list(principal.userId()));
    }

    @PostMapping
    public ResponseEntity<CategoryRule> create(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody @Valid CategoryRule rule) {
        return ResponseEntity.ok(categoryRuleService.create(principal.userId(), rule));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryRule> update(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id,
            @RequestBody CategoryRule rule) {
        return ResponseEntity.ok(categoryRuleService.update(principal.userId(), id, rule));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id) {
        categoryRuleService.delete(principal.userId(), id);
        return ResponseEntity.noContent().build();
    }
}
