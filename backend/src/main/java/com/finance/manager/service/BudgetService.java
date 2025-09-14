package com.finance.manager.service;

import com.finance.manager.model.Budget;
import java.time.YearMonth;
import java.util.List;

public interface BudgetService {
    
    /**
     * Create a new budget
     * @param budget Budget to create
     * @return Created budget
     */
    Budget createBudget(Budget budget);
    
    /**
     * Get all budgets
     * @return List of all budgets
     */
    List<Budget> getAllBudgets();
    
    /**
     * Get budgets by month
     * @param month YearMonth to filter by
     * @return List of budgets for the specified month
     */
    List<Budget> getBudgetsByMonth(YearMonth month);
    
    /**
     * Get budgets for current month
     * @return List of budgets for the current month
     */
    List<Budget> getCurrentMonthBudgets();
    
    /**
     * Get a budget by ID
     * @param id Budget ID
     * @return Budget if found, null otherwise
     */
    Budget getBudgetById(Long id);
    
    /**
     * Update an existing budget
     * @param id Budget ID
     * @param budget Updated budget data
     * @return Updated budget
     */
    Budget updateBudget(Long id, Budget budget);
    
    /**
     * Delete a budget
     * @param id Budget ID
     */
    void deleteBudget(Long id);
    
    /**
     * Update spent amount for a budget
     * @param id Budget ID
     * @param spentAmount New spent amount
     * @return Updated budget
     */
    Budget updateSpentAmount(Long id, Double spentAmount);
}
