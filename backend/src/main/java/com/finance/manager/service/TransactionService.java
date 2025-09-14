package com.finance.manager.service;

import com.finance.manager.model.Transaction;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

public interface TransactionService {
    
    /**
     * Create a new transaction
     * @param transaction Transaction to create
     * @return Created transaction
     */
    Transaction createTransaction(Transaction transaction);
    
    /**
     * Get all transactions
     * @return List of all transactions
     */
    List<Transaction> getAllTransactions();
    
    /**
     * Get transactions by month
     * @param month YearMonth to filter by
     * @return List of transactions for the specified month
     */
    List<Transaction> getTransactionsByMonth(YearMonth month);
    
    /**
     * Get transactions by date range
     * @param startDate Start date for the range
     * @param endDate End date for the range
     * @return List of transactions within the date range
     */
    List<Transaction> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Get transactions for current month
     * @return List of transactions for the current month
     */
    List<Transaction> getCurrentMonthTransactions();
    
    /**
     * Get a transaction by ID
     * @param id Transaction ID
     * @return Transaction if found, null otherwise
     */
    Transaction getTransactionById(Long id);
    
    /**
     * Update an existing transaction
     * @param id Transaction ID
     * @param transaction Updated transaction data
     * @return Updated transaction
     */
    Transaction updateTransaction(Long id, Transaction transaction);
    
    /**
     * Delete a transaction
     * @param id Transaction ID
     */
    void deleteTransaction(Long id);
}
