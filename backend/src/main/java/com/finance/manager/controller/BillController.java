package com.finance.manager.controller;

import com.finance.manager.model.Bill;
import com.finance.manager.security.AuthPrincipal;
import com.finance.manager.service.BillService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bills")
public class BillController {

    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    @GetMapping
    public ResponseEntity<List<Bill>> list(@AuthenticationPrincipal AuthPrincipal principal) {
        return ResponseEntity.ok(billService.getAllBills(principal.userId()));
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
