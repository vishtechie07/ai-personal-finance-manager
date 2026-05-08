package com.finance.manager.service.impl;

import com.finance.manager.model.Transaction;
import com.finance.manager.model.User;
import com.finance.manager.repository.TransactionRepository;
import com.finance.manager.repository.UserRepository;
import com.finance.manager.service.TransactionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Transaction createTransaction(Long userId, Transaction transaction) {
        User owner = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        transaction.setOwner(owner);
        if (transaction.getCategory() == null) {
            transaction.setCategory(Transaction.Category.OTHER);
        }
        return transactionRepository.save(transaction);
    }

    @Override
    public List<Transaction> getAllTransactions(Long userId) {
        return transactionRepository.findByOwner_Id(userId);
    }

    @Override
    public List<Transaction> getTransactionsByMonth(Long userId, YearMonth month) {
        LocalDateTime startOfMonth = month.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = month.atEndOfMonth().atTime(23, 59, 59);
        return transactionRepository.findByOwner_IdAndTransactionDateBetween(userId, startOfMonth, endOfMonth);
    }

    @Override
    public List<Transaction> getTransactionsByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findByOwner_IdAndTransactionDateBetween(userId, startDate, endDate);
    }

    @Override
    public List<Transaction> getCurrentMonthTransactions(Long userId) {
        return getTransactionsByMonth(userId, YearMonth.now());
    }

    @Override
    public Transaction getTransactionById(Long userId, Long id) {
        return transactionRepository.findByIdAndOwner_Id(id, userId).orElse(null);
    }

    @Override
    public Transaction updateTransaction(Long userId, Long id, Transaction transaction) {
        Transaction existing = transactionRepository.findByIdAndOwner_Id(id, userId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        existing.setDescription(transaction.getDescription());
        existing.setAmount(transaction.getAmount());
        existing.setType(transaction.getType());
        existing.setCategory(transaction.getCategory());
        existing.setTransactionDate(transaction.getTransactionDate());

        return transactionRepository.save(existing);
    }

    @Override
    public void deleteTransaction(Long userId, Long id) {
        Transaction transaction = transactionRepository.findByIdAndOwner_Id(id, userId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        transactionRepository.delete(transaction);
    }
}
