package com.finance.manager.repository;

import com.finance.manager.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    @Query("""
            SELECT DISTINCT b FROM Bill b
            LEFT JOIN FETCH b.linkedTransaction
            WHERE b.owner.id = :ownerId
            ORDER BY b.dueDate ASC
            """)
    List<Bill> findByOwner_IdOrderByDueDateAsc(@Param("ownerId") Long ownerId);

    @Modifying
    @Query("""
            DELETE FROM Bill b
            WHERE b.owner.id = :ownerId
            AND (
                LOWER(b.payeeName) LIKE 'smoke test%'
                OR LOWER(b.payeeName) LIKE 'api probe%'
            )
            """)
    int deleteSmokeTestBillsByOwnerId(@Param("ownerId") Long ownerId);

    List<Bill> findByOwner_IdAndPaidFalseAndDueDateBetweenOrderByDueDateAsc(
            Long ownerId, LocalDate start, LocalDate end);

    Optional<Bill> findByIdAndOwner_Id(Long id, Long ownerId);

    void deleteByOwner_Id(Long ownerId);
}
