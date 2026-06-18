package com.finance.manager.service.impl;

import com.finance.manager.model.Bill;
import com.finance.manager.model.User;
import com.finance.manager.repository.BillRepository;
import com.finance.manager.repository.UserRepository;
import com.finance.manager.service.NotificationSyncService;
import com.finance.manager.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillServiceImplTest {

    @Mock BillRepository billRepository;
    @Mock UserRepository userRepository;
    @Mock TransactionService transactionService;
    @Mock NotificationSyncService notificationSyncService;
    @InjectMocks BillServiceImpl billService;

    @Test
    void markPaid_recurringBillRollsDueDateForward() {
        Bill bill = new Bill();
        bill.setId(5L);
        bill.setPayeeName("Rent");
        bill.setAmount(BigDecimal.valueOf(1200));
        bill.setDueDate(LocalDate.of(2026, 6, 1));
        bill.setRecurring(true);
        User owner = new User();
        owner.setId(2L);
        bill.setOwner(owner);

        when(billRepository.findByIdAndOwner_Id(5L, 2L)).thenReturn(Optional.of(bill), Optional.of(bill));
        when(billRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Bill result = billService.markPaid(2L, 5L, Map.of("createTransaction", false));

        assertFalse(result.isPaid());
        assertEquals(LocalDate.of(2026, 7, 1), result.getDueDate());
        verify(notificationSyncService).syncForUser(2L);
    }
}
