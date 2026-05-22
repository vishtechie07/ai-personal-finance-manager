package com.finance.manager.service;

import com.finance.manager.model.AppNotification;
import com.finance.manager.model.Transaction;
import com.finance.manager.model.User;
import com.finance.manager.repository.AppNotificationRepository;
import com.finance.manager.repository.TransactionRepository;
import com.finance.manager.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class SpendingAnomalyService {

    private static final String REF_DUPLICATE = "DUPLICATE_TX";
    private static final String REF_SPIKE = "CATEGORY_SPIKE";

    private final TransactionRepository transactionRepository;
    private final AppNotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public SpendingAnomalyService(
            TransactionRepository transactionRepository,
            AppNotificationRepository notificationRepository,
            UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void syncForUser(Long userId) {
        syncDuplicates(userId);
        syncCategorySpikes(userId);
    }

    private void syncDuplicates(Long userId) {
        LocalDateTime since = LocalDate.now().minusDays(60).atStartOfDay();
        List<Transaction> recent = transactionRepository.findByOwner_Id(userId).stream()
                .filter(t -> t.getTransactionDate() != null && !t.getTransactionDate().isBefore(since))
                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                .toList();

        Map<String, List<Transaction>> buckets = new HashMap<>();
        for (Transaction tx : recent) {
            String key = tx.getTransactionDate().toLocalDate()
                    + "|"
                    + tx.getAmount().setScale(2, RoundingMode.HALF_UP)
                    + "|"
                    + normalize(tx.getDescription());
            buckets.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(tx);
        }

        Set<Long> activeRefs = new HashSet<>();
        for (List<Transaction> group : buckets.values()) {
            if (group.size() < 2) {
                continue;
            }
            Long refId = group.get(0).getId();
            activeRefs.add(refId);
            String title = "Possible duplicate charge";
            String message = String.format(
                    "“%s” for $%s appears %d times on %s. Review if this was entered twice.",
                    truncate(group.get(0).getDescription(), 40),
                    group.get(0).getAmount().toPlainString(),
                    group.size(),
                    group.get(0).getTransactionDate().toLocalDate());
            upsert(userId, AppNotification.NotificationType.SPENDING_ALERT, REF_DUPLICATE, refId, title, message);
        }

        clearStale(userId, REF_DUPLICATE, activeRefs);
    }

    private void syncCategorySpikes(Long userId) {
        YearMonth current = YearMonth.now();
        Set<Long> activeRefs = new HashSet<>();

        for (Transaction.Category category : Transaction.Category.values()) {
            if (category == Transaction.Category.SALARY
                    || category == Transaction.Category.FREELANCE
                    || category == Transaction.Category.INVESTMENTS
                    || category == Transaction.Category.OTHER) {
                continue;
            }

            BigDecimal currentSpend = sumCategory(userId, category, current);
            if (currentSpend.compareTo(BigDecimal.valueOf(50)) < 0) {
                continue;
            }

            BigDecimal priorTotal = BigDecimal.ZERO;
            int priorMonths = 0;
            for (int i = 1; i <= 3; i++) {
                YearMonth m = current.minusMonths(i);
                BigDecimal s = sumCategory(userId, category, m);
                if (s.compareTo(BigDecimal.ZERO) > 0) {
                    priorTotal = priorTotal.add(s);
                    priorMonths++;
                }
            }
            if (priorMonths == 0) {
                continue;
            }
            BigDecimal priorAvg = priorTotal.divide(BigDecimal.valueOf(priorMonths), 2, RoundingMode.HALF_UP);
            if (priorAvg.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            if (currentSpend.compareTo(priorAvg.multiply(BigDecimal.valueOf(2))) <= 0) {
                continue;
            }

            long refId = referenceIdForSpike(category, current);
            activeRefs.add(refId);
            String title = "Unusual spending: " + formatCategory(category);
            String message = String.format(
                    "You spent $%s on %s this month — about %.0fx your recent average of $%s.",
                    currentSpend.toPlainString(),
                    formatCategory(category),
                    currentSpend.divide(priorAvg, 2, RoundingMode.HALF_UP).doubleValue(),
                    priorAvg.toPlainString());
            upsert(userId, AppNotification.NotificationType.SPENDING_ALERT, REF_SPIKE, refId, title, message);
        }

        clearStale(userId, REF_SPIKE, activeRefs);
    }

    private BigDecimal sumCategory(Long userId, Transaction.Category category, YearMonth month) {
        LocalDateTime start = month.atDay(1).atStartOfDay();
        LocalDateTime end = month.atEndOfMonth().atTime(23, 59, 59);
        BigDecimal sum = transactionRepository.sumAmountByOwnerCategoryAndDateRange(
                userId, category, start, end);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    private static long referenceIdForSpike(Transaction.Category category, YearMonth month) {
        return (long) category.ordinal() * 10_000L + month.getYear() * 100L + month.getMonthValue();
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

    private void clearStale(Long userId, String refType, Set<Long> activeRefIds) {
        List<AppNotification> alerts = notificationRepository.findByOwner_IdOrderByCreatedAtDesc(userId).stream()
                .filter(n -> n.getType() == AppNotification.NotificationType.SPENDING_ALERT)
                .filter(n -> refType.equals(n.getReferenceType()))
                .toList();
        for (AppNotification n : alerts) {
            if (n.getReferenceId() != null && !activeRefIds.contains(n.getReferenceId()) && !n.isRead()) {
                notificationRepository.delete(n);
            }
        }
    }

    private static String normalize(String description) {
        if (description == null) {
            return "";
        }
        return description.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }

    private static String formatCategory(Transaction.Category category) {
        return category.name().replace('_', ' ').toLowerCase(Locale.ROOT);
    }
}
