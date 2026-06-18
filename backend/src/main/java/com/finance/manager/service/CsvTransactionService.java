package com.finance.manager.service;

import com.finance.manager.model.Transaction;
import com.finance.manager.model.User;
import com.finance.manager.repository.TransactionRepository;
import com.finance.manager.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class CsvTransactionService {

    public static final int MAX_IMPORT_ROWS = 500;
    private static final String CSV_HEADER = "date,description,amount,type,category";

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRuleService categoryRuleService;
    private final BudgetSpentSyncService budgetSpentSyncService;
    private final NotificationSyncService notificationSyncService;

    public CsvTransactionService(
            TransactionRepository transactionRepository,
            UserRepository userRepository,
            CategoryRuleService categoryRuleService,
            BudgetSpentSyncService budgetSpentSyncService,
            NotificationSyncService notificationSyncService) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.categoryRuleService = categoryRuleService;
        this.budgetSpentSyncService = budgetSpentSyncService;
        this.notificationSyncService = notificationSyncService;
    }

    public String exportCsv(Long userId) {
        List<Transaction> txs = transactionRepository.findByOwner_Id(userId);
        StringBuilder sb = new StringBuilder(CSV_HEADER).append('\n');
        for (Transaction tx : txs) {
            sb.append(escapeCsv(tx.getTransactionDate().toLocalDate().toString())).append(',');
            sb.append(escapeCsv(tx.getDescription())).append(',');
            sb.append(tx.getAmount().setScale(2, RoundingMode.HALF_UP)).append(',');
            sb.append(tx.getType().name()).append(',');
            sb.append(tx.getCategory().name()).append('\n');
        }
        return sb.toString();
    }

    @Transactional
    public int importCsv(Long userId, String csvContent) {
        if (csvContent == null || csvContent.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CSV content is required");
        }
        if (csvContent.length() > 512_000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CSV file too large");
        }
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        List<Transaction> toSave = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new StringReader(csvContent))) {
            String line;
            int row = 0;
            boolean headerSkipped = false;
            while ((line = reader.readLine()) != null) {
                row++;
                if (line.isBlank()) {
                    continue;
                }
                if (!headerSkipped && line.toLowerCase(Locale.ROOT).startsWith("date,")) {
                    headerSkipped = true;
                    continue;
                }
                if (toSave.size() >= MAX_IMPORT_ROWS) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum import rows exceeded");
                }
                String[] parts = parseCsvLine(line);
                if (parts.length < 3) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid CSV at row " + row);
                }
                Transaction tx = parseRow(parts, owner, userId);
                toSave.add(tx);
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to parse CSV");
        }

        transactionRepository.saveAll(toSave);
        budgetSpentSyncService.syncForUserCurrentMonth(userId);
        notificationSyncService.syncForUser(userId);
        return toSave.size();
    }

    private Transaction parseRow(String[] parts, User owner, Long userId) {
        LocalDate date;
        try {
            date = LocalDate.parse(parts[0].trim());
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date: " + parts[0]);
        }
        String description = sanitizeDescription(parts[1]);
        if (description.isBlank() || description.length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid description");
        }
        BigDecimal amount;
        try {
            amount = new BigDecimal(parts[2].trim()).abs().setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid amount");
        }
        if (amount.compareTo(BigDecimal.valueOf(0.01)) < 0 || amount.compareTo(BigDecimal.valueOf(9_999_999)) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount out of range");
        }

        Transaction.TransactionType type = Transaction.TransactionType.EXPENSE;
        if (parts.length > 3 && parts[3] != null && !parts[3].isBlank()) {
            try {
                type = Transaction.TransactionType.valueOf(parts[3].trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid type");
            }
        }

        Transaction.Category category = Transaction.Category.OTHER;
        if (parts.length > 4 && parts[4] != null && !parts[4].isBlank()) {
            try {
                category = Transaction.Category.valueOf(parts[4].trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                category = categoryRuleService.matchCategory(userId, description).orElse(Transaction.Category.OTHER);
            }
        } else {
            category = categoryRuleService.matchCategory(userId, description).orElse(Transaction.Category.OTHER);
        }

        Transaction tx = new Transaction();
        tx.setOwner(owner);
        tx.setDescription(description);
        tx.setAmount(amount);
        tx.setType(type);
        tx.setCategory(category);
        tx.setTransactionDate(date.atStartOfDay());
        return tx;
    }

    private String sanitizeDescription(String raw) {
        if (raw == null) {
            return "";
        }
        String trimmed = raw.trim();
        if (trimmed.startsWith("=") || trimmed.startsWith("+") || trimmed.startsWith("-")
                || trimmed.startsWith("@")) {
            return "'" + trimmed;
        }
        return trimmed.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "");
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields.toArray(String[]::new);
    }
}
