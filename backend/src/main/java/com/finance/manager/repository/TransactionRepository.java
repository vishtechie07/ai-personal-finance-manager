package com.finance.manager.repository;

import com.finance.manager.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    long countByOwner_Id(Long ownerId);

    void deleteByOwner_Id(Long ownerId);

    List<Transaction> findByOwner_Id(Long ownerId);

    List<Transaction> findByOwner_IdAndTransactionDateBetween(Long ownerId, LocalDateTime startDate, LocalDateTime endDate);

    Optional<Transaction> findByIdAndOwner_Id(Long id, Long ownerId);

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t
            WHERE t.owner.id = :ownerId
            AND t.type = 'EXPENSE'
            AND t.category = :category
            AND t.transactionDate BETWEEN :start AND :end
            """)
    BigDecimal sumAmountByOwnerCategoryAndDateRange(
            @Param("ownerId") Long ownerId,
            @Param("category") Transaction.Category category,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
