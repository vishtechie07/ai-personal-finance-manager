package com.finance.manager.repository;

import com.finance.manager.model.CategoryRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRuleRepository extends JpaRepository<CategoryRule, Long> {

    List<CategoryRule> findByOwner_IdOrderByPriorityDescCreatedAtAsc(Long userId);

    long countByOwner_Id(Long userId);

    void deleteByOwner_Id(Long userId);
}
