package com.finance.manager.repository;

import com.finance.manager.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    // Basic CRUD operations are provided by JpaRepository
    
    /**
     * Find budgets that overlap with a date range
     * @param startDate1 Start date for first range
     * @param endDate1 End date for first range
     * @param startDate2 Start date for second range
     * @param endDate2 End date for second range
     * @return List of budgets that overlap with the date ranges
     */
    List<Budget> findByStartDateBetweenOrEndDateBetween(
        LocalDateTime startDate1, LocalDateTime endDate1,
        LocalDateTime startDate2, LocalDateTime endDate2);
}
