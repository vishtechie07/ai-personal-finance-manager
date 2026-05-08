package com.finance.manager.repository;

import com.finance.manager.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByOwner_Id(Long ownerId);

    List<Transaction> findByOwner_IdAndTransactionDateBetween(Long ownerId, LocalDateTime startDate, LocalDateTime endDate);

    Optional<Transaction> findByIdAndOwner_Id(Long id, Long ownerId);
}
