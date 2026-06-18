package com.finance.manager.controller;

import com.finance.manager.model.Transaction;
import com.finance.manager.security.AuthPrincipal;
import com.finance.manager.service.CsvTransactionService;
import com.finance.manager.service.TransactionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final CsvTransactionService csvTransactionService;

    public TransactionController(
            TransactionService transactionService,
            CsvTransactionService csvTransactionService) {
        this.transactionService = transactionService;
        this.csvTransactionService = csvTransactionService;
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportCsv(@AuthenticationPrincipal AuthPrincipal principal) {
        String csv = csvTransactionService.exportCsv(principal.userId());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"transactions.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importCsv(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody Map<String, String> body) {
        String content = body != null ? body.get("csv") : null;
        int imported = csvTransactionService.importCsv(principal.userId(), content);
        return ResponseEntity.ok(Map.of("imported", imported));
    }

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody Transaction transaction) {
        Transaction created = transactionService.createTransaction(principal.userId(), transaction);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions(@AuthenticationPrincipal AuthPrincipal principal) {
        return ResponseEntity.ok(transactionService.getAllTransactions(principal.userId()));
    }

    @GetMapping("/month/{yearMonth}")
    public ResponseEntity<List<Transaction>> getTransactionsByMonth(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable String yearMonth,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Transaction.Category category,
            @RequestParam(required = false) Transaction.TransactionType type,
            @RequestParam(required = false, defaultValue = "dateDesc") String sort) {
        try {
            YearMonth month = YearMonth.parse(yearMonth);
            return ResponseEntity.ok(transactionService.searchTransactions(
                    principal.userId(), month, search, category, type, sort));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/range")
    public ResponseEntity<List<Transaction>> getTransactionsByDateRange(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(transactionService.getTransactionsByDateRange(principal.userId(), startDate, endDate));
    }

    @GetMapping("/current-month")
    public ResponseEntity<List<Transaction>> getCurrentMonthTransactions(@AuthenticationPrincipal AuthPrincipal principal) {
        return ResponseEntity.ok(transactionService.getCurrentMonthTransactions(principal.userId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransaction(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id) {
        Transaction transaction = transactionService.getTransactionById(principal.userId(), id);
        if (transaction == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(transaction);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id,
            @RequestBody Transaction transaction) {
        Transaction updated = transactionService.updateTransaction(principal.userId(), id, transaction);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id) {
        transactionService.deleteTransaction(principal.userId(), id);
        return ResponseEntity.noContent().build();
    }
}
