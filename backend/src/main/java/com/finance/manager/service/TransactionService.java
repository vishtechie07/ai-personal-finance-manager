package com.finance.manager.service;

import com.finance.manager.model.Transaction;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

public interface TransactionService {

    Transaction createTransaction(Long userId, Transaction transaction);

    List<Transaction> getAllTransactions(Long userId);

    List<Transaction> getTransactionsByMonth(Long userId, YearMonth month);

    List<Transaction> searchTransactions(
            Long userId,
            YearMonth month,
            String search,
            Transaction.Category category,
            Transaction.TransactionType type,
            String sort);

    List<Transaction> getTransactionsByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    List<Transaction> getCurrentMonthTransactions(Long userId);

    Transaction getTransactionById(Long userId, Long id);

    Transaction updateTransaction(Long userId, Long id, Transaction transaction);

    void deleteTransaction(Long userId, Long id);
}
