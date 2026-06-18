package com.finance.manager.service;

import com.finance.manager.model.Transaction;
import com.finance.manager.model.User;
import com.finance.manager.repository.TransactionRepository;
import com.finance.manager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CsvTransactionServiceTest {

    @Mock TransactionRepository transactionRepository;
    @Mock UserRepository userRepository;
    @Mock CategoryRuleService categoryRuleService;
    @Mock BudgetSpentSyncService budgetSpentSyncService;
    @Mock NotificationSyncService notificationSyncService;
    @InjectMocks CsvTransactionService csvTransactionService;

    @Test
    void importCsv_sanitizesFormulaInjection() {
        User user = new User();
        user.setId(7L);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(transactionRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        String csv = "date,description,amount,type,category\n"
                + "2026-06-01,=CMD|' /C calc'!A0,12.50,EXPENSE,OTHER\n";

        int count = csvTransactionService.importCsv(7L, csv);
        assertEquals(1, count);
        verify(transactionRepository).saveAll(argThat(list -> {
            Transaction tx = list.iterator().next();
            return tx.getDescription().startsWith("'");
        }));
    }

    @Test
    void importCsv_rejectsOversizedPayload() {
        String huge = "x".repeat(600_000);
        assertThrows(ResponseStatusException.class, () -> csvTransactionService.importCsv(1L, huge));
    }
}
