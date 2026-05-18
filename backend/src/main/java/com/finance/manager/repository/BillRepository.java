package com.finance.manager.repository;

import com.finance.manager.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    List<Bill> findByOwner_IdOrderByDueDateAsc(Long ownerId);

    List<Bill> findByOwner_IdAndPaidFalseAndDueDateBetweenOrderByDueDateAsc(
            Long ownerId, LocalDate start, LocalDate end);

    Optional<Bill> findByIdAndOwner_Id(Long id, Long ownerId);
}
