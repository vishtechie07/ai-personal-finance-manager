package com.finance.manager.service;

import com.finance.manager.model.Bill;

import java.util.List;
import java.util.Map;

public interface BillService {

    List<Bill> getAllBills(Long userId);

    List<Bill> getDueSoon(Long userId, int days);

    Bill getBill(Long userId, Long id);

    Bill createBill(Long userId, Bill bill);

    Bill updateBill(Long userId, Long id, Bill bill);

    void deleteBill(Long userId, Long id);

    Bill markPaid(Long userId, Long id, Map<String, Object> options);
}
