package com.finance.manager.service;

import com.finance.manager.model.Budget;

import java.time.YearMonth;
import java.util.List;

public interface BudgetService {

    Budget createBudget(Long userId, Budget budget);

    List<Budget> getAllBudgets(Long userId);

    List<Budget> getBudgetsByMonth(Long userId, YearMonth month);

    List<Budget> getCurrentMonthBudgets(Long userId);

    Budget getBudgetById(Long userId, Long id);

    Budget updateBudget(Long userId, Long id, Budget budget);

    void deleteBudget(Long userId, Long id);

    Budget updateSpentAmount(Long userId, Long id, Double spentAmount);
}
