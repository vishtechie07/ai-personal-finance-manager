package com.finance.manager.service.impl;

import com.finance.manager.model.Budget;
import com.finance.manager.repository.BudgetRepository;
import com.finance.manager.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
public class BudgetServiceImpl implements BudgetService {
    
    @Autowired
    private BudgetRepository budgetRepository;
    
    @Override
    public Budget createBudget(Budget budget) {
        // Set default values if not provided
        if (budget.getCategory() == null) {
            budget.setCategory(com.finance.manager.model.Transaction.Category.OTHER);
        }
        if (budget.getPeriod() == null) {
            budget.setPeriod(Budget.BudgetPeriod.MONTHLY);
        }
        if (budget.getStartDate() == null) {
            budget.setStartDate(LocalDateTime.now());
        }
        if (budget.getEndDate() == null) {
            // Set end date based on period
            LocalDateTime endDate = budget.getStartDate();
            switch (budget.getPeriod()) {
                case WEEKLY:
                    endDate = endDate.plusWeeks(1);
                    break;
                case MONTHLY:
                    endDate = endDate.plusMonths(1);
                    break;
                case QUARTERLY:
                    endDate = endDate.plusMonths(3);
                    break;
                case YEARLY:
                    endDate = endDate.plusYears(1);
                    break;
            }
            budget.setEndDate(endDate);
        }
        
        return budgetRepository.save(budget);
    }
    
    @Override
    public List<Budget> getAllBudgets() {
        return budgetRepository.findAll();
    }
    
    @Override
    public List<Budget> getBudgetsByMonth(YearMonth month) {
        LocalDateTime startOfMonth = month.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = month.atEndOfMonth().atTime(23, 59, 59);
        return budgetRepository.findByStartDateBetweenOrEndDateBetween(startOfMonth, endOfMonth, startOfMonth, endOfMonth);
    }
    
    @Override
    public List<Budget> getCurrentMonthBudgets() {
        YearMonth currentMonth = YearMonth.now();
        return getBudgetsByMonth(currentMonth);
    }
    
    @Override
    public Budget getBudgetById(Long id) {
        return budgetRepository.findById(id).orElse(null);
    }
    
    @Override
    public Budget updateBudget(Long id, Budget budget) {
        Budget existingBudget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
        
        existingBudget.setName(budget.getName());
        existingBudget.setAmount(budget.getAmount());
        existingBudget.setCategory(budget.getCategory());
        existingBudget.setPeriod(budget.getPeriod());
        existingBudget.setStartDate(budget.getStartDate());
        existingBudget.setEndDate(budget.getEndDate());
        existingBudget.setIsActive(budget.getIsActive());
        
        return budgetRepository.save(existingBudget);
    }
    
    @Override
    public void deleteBudget(Long id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
        budgetRepository.delete(budget);
    }
    
    @Override
    public Budget updateSpentAmount(Long id, Double spentAmount) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
        
        budget.setSpentAmount(BigDecimal.valueOf(spentAmount));
        budget.setRemainingAmount(budget.getAmount().subtract(budget.getSpentAmount()));
        
        return budgetRepository.save(budget);
    }
}
