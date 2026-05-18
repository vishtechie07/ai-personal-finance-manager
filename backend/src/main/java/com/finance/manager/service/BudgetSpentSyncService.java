package com.finance.manager.service;

import com.finance.manager.model.Budget;
import com.finance.manager.repository.BudgetRepository;
import com.finance.manager.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
public class BudgetSpentSyncService {

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;

    public BudgetSpentSyncService(BudgetRepository budgetRepository, TransactionRepository transactionRepository) {
        this.budgetRepository = budgetRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public void syncForUser(Long userId, YearMonth month) {
        LocalDateTime startOfMonth = month.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = month.atEndOfMonth().atTime(23, 59, 59);
        List<Budget> budgets = budgetRepository.findByOwnerOverlappingRange(userId, startOfMonth, endOfMonth);

        for (Budget budget : budgets) {
            LocalDateTime rangeStart = budget.getStartDate().isAfter(startOfMonth)
                    ? budget.getStartDate() : startOfMonth;
            LocalDateTime rangeEnd = budget.getEndDate().isBefore(endOfMonth)
                    ? budget.getEndDate() : endOfMonth;

            BigDecimal spent = transactionRepository.sumAmountByOwnerCategoryAndDateRange(
                    userId,
                    budget.getCategory(),
                    rangeStart,
                    rangeEnd);

            if (spent == null) {
                spent = BigDecimal.ZERO;
            }
            budget.setSpentAmount(spent);
            budget.setRemainingAmount(budget.getAmount().subtract(spent));
            budgetRepository.save(budget);
        }
    }

    @Transactional
    public void syncForUserCurrentMonth(Long userId) {
        syncForUser(userId, YearMonth.now());
    }
}
