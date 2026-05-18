package com.finance.manager.service.impl;

import com.finance.manager.model.Budget;
import com.finance.manager.model.User;
import com.finance.manager.repository.BudgetRepository;
import com.finance.manager.repository.UserRepository;
import com.finance.manager.service.BudgetService;
import com.finance.manager.service.BudgetSpentSyncService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final BudgetSpentSyncService budgetSpentSyncService;

    public BudgetServiceImpl(
            BudgetRepository budgetRepository,
            UserRepository userRepository,
            BudgetSpentSyncService budgetSpentSyncService) {
        this.budgetRepository = budgetRepository;
        this.userRepository = userRepository;
        this.budgetSpentSyncService = budgetSpentSyncService;
    }

    @Override
    public Budget createBudget(Long userId, Budget budget) {
        User owner = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        budget.setOwner(owner);
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
            LocalDateTime endDate = budget.getStartDate();
            switch (budget.getPeriod()) {
                case WEEKLY -> endDate = endDate.plusWeeks(1);
                case MONTHLY -> endDate = endDate.plusMonths(1);
                case QUARTERLY -> endDate = endDate.plusMonths(3);
                case YEARLY -> endDate = endDate.plusYears(1);
            }
            budget.setEndDate(endDate);
        }

        return budgetRepository.save(budget);
    }

    @Override
    public List<Budget> getAllBudgets(Long userId) {
        budgetSpentSyncService.syncForUserCurrentMonth(userId);
        return budgetRepository.findByOwner_Id(userId);
    }

    @Override
    public List<Budget> getBudgetsByMonth(Long userId, YearMonth month) {
        LocalDateTime startOfMonth = month.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = month.atEndOfMonth().atTime(23, 59, 59);
        return budgetRepository.findByOwnerOverlappingRange(userId, startOfMonth, endOfMonth);
    }

    @Override
    public List<Budget> getCurrentMonthBudgets(Long userId) {
        return getBudgetsByMonth(userId, YearMonth.now());
    }

    @Override
    public Budget getBudgetById(Long userId, Long id) {
        return budgetRepository.findByIdAndOwner_Id(id, userId).orElse(null);
    }

    @Override
    public Budget updateBudget(Long userId, Long id, Budget budget) {
        Budget existing = budgetRepository.findByIdAndOwner_Id(id, userId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));

        existing.setName(budget.getName());
        existing.setAmount(budget.getAmount());
        existing.setCategory(budget.getCategory());
        existing.setPeriod(budget.getPeriod());
        existing.setStartDate(budget.getStartDate());
        existing.setEndDate(budget.getEndDate());
        existing.setIsActive(budget.getIsActive());

        return budgetRepository.save(existing);
    }

    @Override
    public void deleteBudget(Long userId, Long id) {
        Budget budget = budgetRepository.findByIdAndOwner_Id(id, userId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
        budgetRepository.delete(budget);
    }

    @Override
    public Budget updateSpentAmount(Long userId, Long id, Double spentAmount) {
        Budget budget = budgetRepository.findByIdAndOwner_Id(id, userId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));

        budget.setSpentAmount(BigDecimal.valueOf(spentAmount));
        budget.setRemainingAmount(budget.getAmount().subtract(budget.getSpentAmount()));

        return budgetRepository.save(budget);
    }
}
