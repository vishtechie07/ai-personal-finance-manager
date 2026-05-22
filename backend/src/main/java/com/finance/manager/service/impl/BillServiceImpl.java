package com.finance.manager.service.impl;

import com.finance.manager.model.Bill;
import com.finance.manager.model.Transaction;
import com.finance.manager.model.User;
import com.finance.manager.repository.BillRepository;
import com.finance.manager.repository.UserRepository;
import com.finance.manager.service.BillService;
import com.finance.manager.service.NotificationSyncService;
import com.finance.manager.service.TransactionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class BillServiceImpl implements BillService {

    private final BillRepository billRepository;
    private final UserRepository userRepository;
    private final TransactionService transactionService;
    private final NotificationSyncService notificationSyncService;

    public BillServiceImpl(
            BillRepository billRepository,
            UserRepository userRepository,
            TransactionService transactionService,
            NotificationSyncService notificationSyncService) {
        this.billRepository = billRepository;
        this.userRepository = userRepository;
        this.transactionService = transactionService;
        this.notificationSyncService = notificationSyncService;
    }

    @Override
    public List<Bill> getAllBills(Long userId) {
        return billRepository.findByOwner_IdOrderByDueDateAsc(userId);
    }

    @Override
    public List<Bill> getDueSoon(Long userId, int days) {
        LocalDate today = LocalDate.now();
        return billRepository.findByOwner_IdAndPaidFalseAndDueDateBetweenOrderByDueDateAsc(
                userId, today, today.plusDays(days));
    }

    @Override
    public Bill getBill(Long userId, Long id) {
        return billRepository.findByIdAndOwner_Id(id, userId).orElse(null);
    }

    @Override
    @Transactional
    public Bill createBill(Long userId, Bill bill) {
        User owner = userRepository.findById(userId).orElseThrow();
        bill.setOwner(owner);
        Bill saved = billRepository.save(bill);
        notificationSyncService.syncForUser(userId);
        return saved;
    }

    @Override
    @Transactional
    public Bill updateBill(Long userId, Long id, Bill bill) {
        Bill existing = billRepository.findByIdAndOwner_Id(id, userId)
                .orElseThrow(() -> new RuntimeException("Bill not found"));
        existing.setPayeeName(bill.getPayeeName());
        existing.setAmount(bill.getAmount());
        existing.setDueDate(bill.getDueDate());
        existing.setPaid(bill.isPaid());
        if (bill.isPaid() && existing.getPaidAt() == null) {
            existing.setPaidAt(LocalDateTime.now());
        }
        Bill saved = billRepository.save(existing);
        notificationSyncService.syncForUser(userId);
        return saved;
    }

    @Override
    @Transactional
    public void deleteBill(Long userId, Long id) {
        Bill bill = billRepository.findByIdAndOwner_Id(id, userId)
                .orElseThrow(() -> new RuntimeException("Bill not found"));
        billRepository.delete(bill);
        notificationSyncService.syncForUser(userId);
    }

    @Override
    @Transactional
    public Bill markPaid(Long userId, Long id, Map<String, Object> options) {
        Bill bill = billRepository.findByIdAndOwner_Id(id, userId)
                .orElseThrow(() -> new RuntimeException("Bill not found"));
        bill.setPaid(true);
        bill.setPaidAt(LocalDateTime.now());

        boolean createTx = options != null && Boolean.TRUE.equals(options.get("createTransaction"));
        if (createTx && bill.getLinkedTransaction() == null) {
            Transaction tx = new Transaction();
            tx.setDescription(bill.getPayeeName());
            tx.setAmount(bill.getAmount());
            tx.setType(Transaction.TransactionType.EXPENSE);
            tx.setCategory(Transaction.Category.OTHER);
            tx.setTransactionDate(LocalDateTime.now());
            Transaction created = transactionService.createTransaction(userId, tx);
            bill.setLinkedTransaction(created);
        }

        billRepository.save(bill);
        notificationSyncService.syncForUser(userId);
        return billRepository.findByIdAndOwner_Id(id, userId).orElse(bill);
    }
}
