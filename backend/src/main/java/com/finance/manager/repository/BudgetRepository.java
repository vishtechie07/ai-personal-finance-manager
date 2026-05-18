package com.finance.manager.repository;

import com.finance.manager.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    long countByOwner_Id(Long ownerId);

    void deleteByOwner_Id(Long ownerId);

    List<Budget> findByOwner_Id(Long ownerId);

    @Query("""
            SELECT b FROM Budget b
            WHERE b.owner.id = :ownerId
            AND b.startDate <= :rangeEnd
            AND b.endDate >= :rangeStart
            """)
    List<Budget> findByOwnerOverlappingRange(
            @Param("ownerId") Long ownerId,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd);

    Optional<Budget> findByIdAndOwner_Id(Long id, Long ownerId);
}
