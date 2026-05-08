package com.finance.manager.controller;

import com.finance.manager.model.Budget;
import com.finance.manager.security.AuthPrincipal;
import com.finance.manager.service.BudgetService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    public ResponseEntity<Budget> createBudget(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody Budget budget) {
        return ResponseEntity.ok(budgetService.createBudget(principal.userId(), budget));
    }

    @GetMapping
    public ResponseEntity<List<Budget>> getAllBudgets(@AuthenticationPrincipal AuthPrincipal principal) {
        return ResponseEntity.ok(budgetService.getAllBudgets(principal.userId()));
    }

    @GetMapping("/month/{yearMonth}")
    public ResponseEntity<List<Budget>> getBudgetsByMonth(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable String yearMonth) {
        try {
            YearMonth month = YearMonth.parse(yearMonth);
            return ResponseEntity.ok(budgetService.getBudgetsByMonth(principal.userId(), month));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/current-month")
    public ResponseEntity<List<Budget>> getCurrentMonthBudgets(@AuthenticationPrincipal AuthPrincipal principal) {
        return ResponseEntity.ok(budgetService.getCurrentMonthBudgets(principal.userId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Budget> getBudget(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id) {
        Budget budget = budgetService.getBudgetById(principal.userId(), id);
        if (budget == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(budget);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Budget> updateBudget(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id,
            @RequestBody Budget budget) {
        return ResponseEntity.ok(budgetService.updateBudget(principal.userId(), id, budget));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id) {
        budgetService.deleteBudget(principal.userId(), id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/spent")
    public ResponseEntity<Budget> updateSpentAmount(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        Double spentAmount = Double.valueOf(request.get("spentAmount").toString());
        return ResponseEntity.ok(budgetService.updateSpentAmount(principal.userId(), id, spentAmount));
    }
}
