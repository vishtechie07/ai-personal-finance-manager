package com.finance.manager.service;

import com.finance.manager.model.Transaction;
import com.finance.manager.model.User;
import com.finance.manager.repository.CategoryRuleRepository;
import com.finance.manager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryRuleServiceTest {

    @Mock CategoryRuleRepository categoryRuleRepository;
    @Mock UserRepository userRepository;
    @InjectMocks CategoryRuleService categoryRuleService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
    }

    @Test
    void matchCategory_findsHighestPriorityRule() {
        var rule = new com.finance.manager.model.CategoryRule();
        rule.setPattern("uber");
        rule.setCategory(Transaction.Category.TRANSPORTATION);
        rule.setPriority(10);
        when(categoryRuleRepository.findByOwner_IdOrderByPriorityDescCreatedAtAsc(1L))
                .thenReturn(java.util.List.of(rule));

        Optional<Transaction.Category> result = categoryRuleService.matchCategory(1L, "UBER ride downtown");
        assertEquals(Transaction.Category.TRANSPORTATION, result.orElseThrow());
    }

    @Test
    void create_rejectsUnsafePattern() {
        var rule = new com.finance.manager.model.CategoryRule();
        rule.setPattern("<script>");
        rule.setCategory(Transaction.Category.OTHER);

        assertThrows(ResponseStatusException.class, () -> categoryRuleService.create(1L, rule));
        verify(categoryRuleRepository, never()).save(any());
    }

    @Test
    void create_persistsNormalizedPattern() {
        when(categoryRuleRepository.countByOwner_Id(1L)).thenReturn(0L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRuleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var rule = new com.finance.manager.model.CategoryRule();
        rule.setPattern("  Netflix  ");
        rule.setCategory(Transaction.Category.ENTERTAINMENT);

        categoryRuleService.create(1L, rule);

        ArgumentCaptor<com.finance.manager.model.CategoryRule> captor =
                ArgumentCaptor.forClass(com.finance.manager.model.CategoryRule.class);
        verify(categoryRuleRepository).save(captor.capture());
        assertEquals("Netflix", captor.getValue().getPattern());
    }
}
