package com.finance.manager.service.impl;

import com.finance.manager.model.Transaction;
import com.finance.manager.model.User;
import com.finance.manager.repository.TransactionRepository;
import com.finance.manager.repository.TransactionSpecifications;
import com.finance.manager.repository.UserRepository;
import com.finance.manager.service.BudgetSpentSyncService;
import com.finance.manager.service.NotificationSyncService;
import com.finance.manager.service.TransactionService;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final BudgetSpentSyncService budgetSpentSyncService;
    private final NotificationSyncService notificationSyncService;

    public TransactionServiceImpl(
            TransactionRepository transactionRepository,
            UserRepository userRepository,
            BudgetSpentSyncService budgetSpentSyncService,
            NotificationSyncService notificationSyncService) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.budgetSpentSyncService = budgetSpentSyncService;
        this.notificationSyncService = notificationSyncService;
    }

    @Override
    @Transactional
    public Transaction createTransaction(Long userId, Transaction transaction) {
        User owner = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        transaction.setOwner(owner);
        if (transaction.getCategory() == null) {
            transaction.setCategory(Transaction.Category.OTHER);
        }
        Transaction saved = transactionRepository.save(transaction);
        afterMutation(userId, saved.getTransactionDate());
        return saved;
    }

    @Override
    public List<Transaction> getAllTransactions(Long userId) {
        return transactionRepository.findByOwner_Id(userId);
    }

    @Override
    public List<Transaction> getTransactionsByMonth(Long userId, YearMonth month) {
        return searchTransactions(userId, month, null, null, null, "dateDesc");
    }

    @Override
    public List<Transaction> searchTransactions(
            Long userId,
            YearMonth month,
            String search,
            Transaction.Category category,
            Transaction.TransactionType type,
            String sort) {
        LocalDateTime startOfMonth = month.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = month.atEndOfMonth().atTime(23, 59, 59);

        Specification<Transaction> spec = Specification
                .where(TransactionSpecifications.forOwner(userId))
                .and(TransactionSpecifications.betweenDates(startOfMonth, endOfMonth));

        if (search != null && !search.isBlank()) {
            spec = spec.and(TransactionSpecifications.descriptionContains(search.trim()));
        }
        if (category != null) {
            spec = spec.and(TransactionSpecifications.hasCategory(category));
        }
        if (type != null) {
            spec = spec.and(TransactionSpecifications.hasType(type));
        }

        return transactionRepository.findAll(spec, resolveSort(sort));
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
    @Transactional
    public Transaction updateTransaction(Long userId, Long id, Transaction transaction) {
        Transaction existing = transactionRepository.findByIdAndOwner_Id(id, userId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        existing.setDescription(transaction.getDescription());
        existing.setAmount(transaction.getAmount());
        existing.setType(transaction.getType());
        existing.setCategory(transaction.getCategory());
        existing.setTransactionDate(transaction.getTransactionDate());

        Transaction saved = transactionRepository.save(existing);
        afterMutation(userId, saved.getTransactionDate());
        return saved;
    }

    @Override
    @Transactional
    public void deleteTransaction(Long userId, Long id) {
        Transaction transaction = transactionRepository.findByIdAndOwner_Id(id, userId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        YearMonth month = YearMonth.from(transaction.getTransactionDate());
        transactionRepository.delete(transaction);
        budgetSpentSyncService.syncForUser(userId, month);
        notificationSyncService.syncForUser(userId);
    }

    private void afterMutation(Long userId, LocalDateTime transactionDate) {
        YearMonth month = YearMonth.from(transactionDate);
        budgetSpentSyncService.syncForUser(userId, month);
        notificationSyncService.syncForUser(userId);
    }

    private Sort resolveSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "transactionDate");
        }
        return switch (sort) {
            case "dateAsc" -> Sort.by(Sort.Direction.ASC, "transactionDate");
            case "amountDesc" -> Sort.by(Sort.Direction.DESC, "amount");
            case "amountAsc" -> Sort.by(Sort.Direction.ASC, "amount");
            default -> Sort.by(Sort.Direction.DESC, "transactionDate");
        };
    }
}
