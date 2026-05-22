package com.finance.manager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.finance.manager.model.Budget;
import com.finance.manager.model.Transaction;
import com.finance.manager.repository.BudgetRepository;
import com.finance.manager.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FinancialMonthSummaryService {

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final ObjectMapper objectMapper;

    public FinancialMonthSummaryService(
            TransactionRepository transactionRepository,
            BudgetRepository budgetRepository,
            ObjectMapper objectMapper) {
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
        this.objectMapper = objectMapper;
    }

    public String buildSummaryJson(Long userId, YearMonth month) {
        LocalDateTime start = month.atDay(1).atStartOfDay();
        LocalDateTime end = month.atEndOfMonth().atTime(23, 59, 59);

        List<Transaction> transactions =
                transactionRepository.findByOwner_IdAndTransactionDateBetween(userId, start, end);
        List<Budget> budgets = budgetRepository.findByOwnerOverlappingRange(userId, start, end);

        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expenses = BigDecimal.ZERO;
        Map<String, BigDecimal> categorySpend = new HashMap<>();

        for (Transaction tx : transactions) {
            if (tx.getType() == Transaction.TransactionType.INCOME) {
                income = income.add(tx.getAmount());
            } else if (tx.getType() == Transaction.TransactionType.EXPENSE) {
                expenses = expenses.add(tx.getAmount());
                String cat = tx.getCategory() != null ? tx.getCategory().name() : "OTHER";
                categorySpend.merge(cat, tx.getAmount(), BigDecimal::add);
            }
        }

        ObjectNode root = objectMapper.createObjectNode();
        root.put("yearMonth", month.toString());
        root.put("totalIncome", income.setScale(2, RoundingMode.HALF_UP).toPlainString());
        root.put("totalExpenses", expenses.setScale(2, RoundingMode.HALF_UP).toPlainString());
        root.put(
                "net",
                income.subtract(expenses).setScale(2, RoundingMode.HALF_UP).toPlainString());
        root.put("transactionCount", transactions.size());

        ArrayNode topCategories = root.putArray("topExpenseCategories");
        categorySpend.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue(Comparator.reverseOrder()))
                .limit(6)
                .forEach(e -> {
                    ObjectNode row = topCategories.addObject();
                    row.put("category", e.getKey());
                    row.put("amount", e.getValue().setScale(2, RoundingMode.HALF_UP).toPlainString());
                });

        ArrayNode budgetRows = root.putArray("budgets");
        for (Budget b : budgets) {
            ObjectNode row = budgetRows.addObject();
            row.put("name", b.getName());
            row.put("limit", b.getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString());
            row.put("spent", b.getSpentAmount().setScale(2, RoundingMode.HALF_UP).toPlainString());
            if (b.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal pct = b.getSpentAmount()
                        .divide(b.getAmount(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                row.put("percentUsed", pct.setScale(0, RoundingMode.HALF_UP).intValue());
            } else {
                row.put("percentUsed", 0);
            }
        }

        try {
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            return root.toString();
        }
    }
}
