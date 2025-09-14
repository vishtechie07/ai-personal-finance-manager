package com.finance.manager.service.impl;

import com.finance.manager.model.Transaction;
import com.finance.manager.repository.TransactionRepository;
import com.finance.manager.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Override
    public Transaction createTransaction(Transaction transaction) {
        // Set default values if not provided
        if (transaction.getCategory() == null) {
            transaction.setCategory(Transaction.Category.OTHER);
        }
        
        return transactionRepository.save(transaction);
    }
    
    @Override
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }
    
    @Override
    public List<Transaction> getTransactionsByMonth(YearMonth month) {
        LocalDateTime startOfMonth = month.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = month.atEndOfMonth().atTime(23, 59, 59);
        return transactionRepository.findByTransactionDateBetween(startOfMonth, endOfMonth);
    }
    
    @Override
    public List<Transaction> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findByTransactionDateBetween(startDate, endDate);
    }
    
    @Override
    public List<Transaction> getCurrentMonthTransactions() {
        YearMonth currentMonth = YearMonth.now();
        return getTransactionsByMonth(currentMonth);
    }
    
    @Override
    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id).orElse(null);
    }
    
    @Override
    public Transaction updateTransaction(Long id, Transaction transaction) {
        Transaction existingTransaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        
        existingTransaction.setDescription(transaction.getDescription());
        existingTransaction.setAmount(transaction.getAmount());
        existingTransaction.setType(transaction.getType());
        existingTransaction.setCategory(transaction.getCategory());
        existingTransaction.setTransactionDate(transaction.getTransactionDate());
        
        return transactionRepository.save(existingTransaction);
    }
    
    @Override
    public void deleteTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        transactionRepository.delete(transaction);
    }
}
