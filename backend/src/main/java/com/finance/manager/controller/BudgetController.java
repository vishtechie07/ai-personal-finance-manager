package com.finance.manager.controller;

import com.finance.manager.model.Budget;
import com.finance.manager.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/budgets")
public class BudgetController {
    
    @Autowired
    private BudgetService budgetService;
    
    /**
     * Create a new budget
     */
    @PostMapping
    public ResponseEntity<Budget> createBudget(@RequestBody Budget budget) {
        Budget createdBudget = budgetService.createBudget(budget);
        return ResponseEntity.ok(createdBudget);
    }
    
    /**
     * Get all budgets
     */
    @GetMapping
    public ResponseEntity<List<Budget>> getAllBudgets() {
        List<Budget> budgets = budgetService.getAllBudgets();
        return ResponseEntity.ok(budgets);
    }
    
    /**
     * Get budgets by month (YYYY-MM format)
     */
    @GetMapping("/month/{yearMonth}")
    public ResponseEntity<List<Budget>> getBudgetsByMonth(@PathVariable String yearMonth) {
        try {
            YearMonth month = YearMonth.parse(yearMonth);
            List<Budget> budgets = budgetService.getBudgetsByMonth(month);
            return ResponseEntity.ok(budgets);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get budgets for current month
     */
    @GetMapping("/current-month")
    public ResponseEntity<List<Budget>> getCurrentMonthBudgets() {
        List<Budget> budgets = budgetService.getCurrentMonthBudgets();
        return ResponseEntity.ok(budgets);
    }
    
    /**
     * Get a specific budget
     */
    @GetMapping("/{id}")
    public ResponseEntity<Budget> getBudget(@PathVariable Long id) {
        Budget budget = budgetService.getBudgetById(id);
        if (budget == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(budget);
    }
    
    /**
     * Update a budget
     */
    @PutMapping("/{id}")
    public ResponseEntity<Budget> updateBudget(
            @PathVariable Long id,
            @RequestBody Budget budget) {
        Budget updatedBudget = budgetService.updateBudget(id, budget);
        return ResponseEntity.ok(updatedBudget);
    }
    
    /**
     * Delete a budget
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Update spent amount for a budget
     */
    @PutMapping("/{id}/spent")
    public ResponseEntity<Budget> updateSpentAmount(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        Double spentAmount = Double.valueOf(request.get("spentAmount").toString());
        Budget updatedBudget = budgetService.updateSpentAmount(id, spentAmount);
        return ResponseEntity.ok(updatedBudget);
    }
}
