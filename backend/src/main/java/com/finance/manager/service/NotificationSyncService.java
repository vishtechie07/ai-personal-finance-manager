package com.finance.manager.service;

import com.finance.manager.model.AppNotification;
import com.finance.manager.model.Bill;
import com.finance.manager.model.Budget;
import com.finance.manager.model.User;
import com.finance.manager.repository.AppNotificationRepository;
import com.finance.manager.repository.BillRepository;
import com.finance.manager.repository.BudgetRepository;
import com.finance.manager.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class NotificationSyncService {

    private static final String REF_BUDGET = "BUDGET";
    private static final String REF_BILL = "BILL";

    private final AppNotificationRepository notificationRepository;
    private final BudgetRepository budgetRepository;
    private final BillRepository billRepository;
    private final UserRepository userRepository;
    private final BudgetSpentSyncService budgetSpentSyncService;

    public NotificationSyncService(
            AppNotificationRepository notificationRepository,
            BudgetRepository budgetRepository,
            BillRepository billRepository,
            UserRepository userRepository,
            BudgetSpentSyncService budgetSpentSyncService) {
        this.notificationRepository = notificationRepository;
        this.budgetRepository = budgetRepository;
        this.billRepository = billRepository;
        this.userRepository = userRepository;
        this.budgetSpentSyncService = budgetSpentSyncService;
    }

    @Transactional
    public void syncForUser(Long userId) {
        budgetSpentSyncService.syncForUserCurrentMonth(userId);
        syncBudgetWarnings(userId);
        syncBillDue(userId);
    }

    private void syncBudgetWarnings(Long userId) {
        YearMonth month = YearMonth.now();
        LocalDateTime start = month.atDay(1).atStartOfDay();
        LocalDateTime end = month.atEndOfMonth().atTime(23, 59, 59);
        List<Budget> budgets = budgetRepository.findByOwnerOverlappingRange(userId, start, end);

        for (Budget budget : budgets) {
            if (budget.getAmount() == null || budget.getAmount().compareTo(BigDecimal.ZERO) == 0) {
                clearUnread(userId, AppNotification.NotificationType.BUDGET_WARNING, REF_BUDGET, budget.getId());
                continue;
            }
            BigDecimal pct = budget.getSpentAmount()
                    .divide(budget.getAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            if (pct.compareTo(BigDecimal.valueOf(90)) >= 0) {
                String title = budget.getName() + " budget";
                String message = pct.compareTo(BigDecimal.valueOf(100)) >= 0
                        ? "You are over budget for " + budget.getName() + "."
                        : String.format("You have used %.0f%% of your %s budget.", pct.doubleValue(), budget.getName());
                upsert(userId, AppNotification.NotificationType.BUDGET_WARNING, REF_BUDGET, budget.getId(), title, message);
            } else {
                clearUnread(userId, AppNotification.NotificationType.BUDGET_WARNING, REF_BUDGET, budget.getId());
            }
        }
    }

    private void syncBillDue(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate horizon = today.plusDays(7);
        List<Bill> bills = billRepository.findByOwner_IdAndPaidFalseAndDueDateBetweenOrderByDueDateAsc(
                userId, today, horizon);

        for (Bill bill : bills) {
            long days = ChronoUnit.DAYS.between(today, bill.getDueDate());
            String when = days == 0 ? "today" : days == 1 ? "tomorrow" : "in " + days + " days";
            String title = "Bill due: " + bill.getPayeeName();
            String message = String.format("$%s due %s.", bill.getAmount().toPlainString(), when);
            upsert(userId, AppNotification.NotificationType.BILL_DUE, REF_BILL, bill.getId(), title, message);
        }
    }

    private void upsert(
            Long userId,
            AppNotification.NotificationType type,
            String refType,
            Long refId,
            String title,
            String message) {
        List<AppNotification> existing = notificationRepository.findByOwner_IdAndTypeAndReferenceTypeAndReferenceId(
                userId, type, refType, refId);
        if (!existing.isEmpty()) {
            AppNotification n = existing.get(0);
            n.setTitle(title);
            n.setMessage(message);
            notificationRepository.save(n);
            return;
        }
        User owner = userRepository.findById(userId).orElseThrow();
        AppNotification n = new AppNotification();
        n.setOwner(owner);
        n.setType(type);
        n.setReferenceType(refType);
        n.setReferenceId(refId);
        n.setTitle(title);
        n.setMessage(message);
        n.setRead(false);
        notificationRepository.save(n);
    }

    private void clearUnread(
            Long userId,
            AppNotification.NotificationType type,
            String refType,
            Long refId) {
        notificationRepository.deleteByOwner_IdAndTypeAndReferenceTypeAndReferenceIdAndReadFalse(
                userId, type, refType, refId);
    }
}
