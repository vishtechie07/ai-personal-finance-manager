package com.finance.manager.controller;

import com.finance.manager.model.Bill;
import com.finance.manager.security.AuthPrincipal;
import com.finance.manager.service.BillService;
import com.finance.manager.service.RecurringBillDetectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bills")
public class BillController {

    private final BillService billService;
    private final RecurringBillDetectionService recurringBillDetectionService;

    public BillController(BillService billService, RecurringBillDetectionService recurringBillDetectionService) {
        this.billService = billService;
        this.recurringBillDetectionService = recurringBillDetectionService;
    }

    @GetMapping
    public ResponseEntity<List<Bill>> list(@AuthenticationPrincipal AuthPrincipal principal) {
        return ResponseEntity.ok(billService.getAllBills(principal.userId()));
    }

    @GetMapping("/detect-recurring")
    public ResponseEntity<List<Map<String, Object>>> detectRecurring(
            @AuthenticationPrincipal AuthPrincipal principal) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (RecurringBillDetectionService.RecurringSuggestion s :
                recurringBillDetectionService.detect(principal.userId())) {
            Map<String, Object> row = new HashMap<>();
            row.put("payeeName", s.payeeName());
            row.put("amount", s.amount());
            row.put("suggestedDueDate", s.suggestedDueDate().toString());
            row.put("occurrenceCount", s.occurrenceCount());
            row.put("merchantKey", s.merchantKey());
            rows.add(row);
        }
        return ResponseEntity.ok(rows);
    }

    @GetMapping("/due-soon")
    public ResponseEntity<List<Bill>> dueSoon(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(billService.getDueSoon(principal.userId(), days));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bill> get(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id) {
        Bill bill = billService.getBill(principal.userId(), id);
        if (bill == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(bill);
    }

    @PostMapping
    public ResponseEntity<Bill> create(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody Bill bill) {
        return ResponseEntity.ok(billService.createBill(principal.userId(), bill));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Bill> update(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id,
            @RequestBody Bill bill) {
        return ResponseEntity.ok(billService.updateBill(principal.userId(), id, bill));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id) {
        billService.deleteBill(principal.userId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/mark-paid")
    public ResponseEntity<Bill> markPaid(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body) {
        return ResponseEntity.ok(billService.markPaid(principal.userId(), id, body));
    }
}
