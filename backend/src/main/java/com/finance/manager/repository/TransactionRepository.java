package com.finance.manager.repository;

import com.finance.manager.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // Basic CRUD operations are provided by JpaRepository
    
    /**
     * Find transactions between two dates
     * @param startDate Start date
     * @param endDate End date
     * @return List of transactions within the date range
     */
    List<Transaction> findByTransactionDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}
