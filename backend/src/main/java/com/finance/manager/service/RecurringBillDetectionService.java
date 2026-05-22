package com.finance.manager.service;

import com.finance.manager.model.Transaction;
import com.finance.manager.repository.BillRepository;
import com.finance.manager.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RecurringBillDetectionService {

    private final TransactionRepository transactionRepository;
    private final BillRepository billRepository;

    public RecurringBillDetectionService(
            TransactionRepository transactionRepository,
            BillRepository billRepository) {
        this.transactionRepository = transactionRepository;
        this.billRepository = billRepository;
    }

    public List<RecurringSuggestion> detect(Long userId) {
        YearMonth end = YearMonth.now();
        YearMonth start = end.minusMonths(5);
        LocalDateTime rangeStart = start.atDay(1).atStartOfDay();
        LocalDateTime rangeEnd = end.atEndOfMonth().atTime(23, 59, 59);

        List<Transaction> expenses = transactionRepository
                .findByOwner_IdAndTransactionDateBetween(userId, rangeStart, rangeEnd)
                .stream()
                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                .toList();

        Set<String> existingPayees = billRepository.findByOwner_IdOrderByDueDateAsc(userId).stream()
                .map(b -> normalizePayee(b.getPayeeName()))
                .collect(Collectors.toSet());

        Map<String, List<Transaction>> groups = new HashMap<>();
        for (Transaction tx : expenses) {
            String key = normalizeMerchant(tx.getDescription());
            if (key.length() < 3) {
                continue;
            }
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(tx);
        }

        List<RecurringSuggestion> suggestions = new ArrayList<>();
        for (Map.Entry<String, List<Transaction>> entry : groups.entrySet()) {
            List<Transaction> group = entry.getValue();
            if (group.size() < 2) {
                continue;
            }
            if (existingPayees.contains(entry.getKey())) {
                continue;
            }

            Set<YearMonth> months = group.stream()
                    .map(t -> YearMonth.from(t.getTransactionDate()))
                    .collect(Collectors.toSet());
            if (months.size() < 2) {
                continue;
            }

            List<BigDecimal> amounts = group.stream().map(Transaction::getAmount).sorted().toList();
            BigDecimal median = amounts.get(amounts.size() / 2);
            boolean amountsSimilar = amounts.stream().allMatch(a -> withinPercent(a, median, 20));
            if (!amountsSimilar) {
                continue;
            }

            Transaction latest = group.stream()
                    .max(Comparator.comparing(Transaction::getTransactionDate))
                    .orElseThrow();
            LocalDate lastDate = latest.getTransactionDate().toLocalDate();
            LocalDate suggestedDue = lastDate.plusMonths(1);
            if (suggestedDue.isBefore(LocalDate.now())) {
                suggestedDue = LocalDate.now().plusDays(7);
            }

            String displayName = toDisplayName(group.get(0).getDescription());
            suggestions.add(new RecurringSuggestion(
                    displayName,
                    median.setScale(2, RoundingMode.HALF_UP),
                    suggestedDue,
                    group.size(),
                    entry.getKey()));
        }

        suggestions.sort(Comparator.comparing(RecurringSuggestion::occurrenceCount).reversed());
        return suggestions.size() > 10 ? suggestions.subList(0, 10) : suggestions;
    }

    private static boolean withinPercent(BigDecimal value, BigDecimal target, int percent) {
        if (target.compareTo(BigDecimal.ZERO) == 0) {
            return value.compareTo(BigDecimal.ZERO) == 0;
        }
        BigDecimal diff = value.subtract(target).abs();
        BigDecimal threshold = target.multiply(BigDecimal.valueOf(percent))
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return diff.compareTo(threshold) <= 0;
    }

    private static String normalizeMerchant(String description) {
        if (description == null) {
            return "";
        }
        return description.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static String normalizePayee(String payee) {
        return payee == null ? "" : payee.toLowerCase(Locale.ROOT).trim();
    }

    private static String toDisplayName(String description) {
        if (description == null || description.isBlank()) {
            return "Recurring payment";
        }
        String trimmed = description.trim();
        if (trimmed.length() <= 40) {
            return trimmed;
        }
        return trimmed.substring(0, 37) + "...";
    }

    public record RecurringSuggestion(
            String payeeName,
            BigDecimal amount,
            LocalDate suggestedDueDate,
            int occurrenceCount,
            String merchantKey) {}
}
