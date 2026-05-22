package com.finance.manager.service;

import com.finance.manager.model.Budget;
import com.finance.manager.model.Transaction;
import com.finance.manager.model.User;
import com.finance.manager.repository.AppNotificationRepository;
import com.finance.manager.repository.BillRepository;
import com.finance.manager.repository.BudgetRepository;
import com.finance.manager.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Service
public class DemoSeedService {

    /** Seeded trial account — see docs/DEMO_CREDENTIALS.md */
    public static final String DEMO_USERNAME = "spendsense";
    public static final String DEMO_PASSWORD = "TrySpend2026!";
    public static final String DEMO_EMAIL = "trial@spendsense.app";
    public static final String DEMO_FIRST_NAME = "SpendSense";
    public static final String DEMO_LAST_NAME = "Trial";

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final BillRepository billRepository;
    private final AppNotificationRepository notificationRepository;
    private final ReceiptStorageService receiptStorageService;

    public DemoSeedService(
            TransactionRepository transactionRepository,
            BudgetRepository budgetRepository,
            BillRepository billRepository,
            AppNotificationRepository notificationRepository,
            ReceiptStorageService receiptStorageService) {
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
        this.billRepository = billRepository;
        this.notificationRepository = notificationRepository;
        this.receiptStorageService = receiptStorageService;
    }

    /**
     * Idempotent: only seeds when this user has no transactions yet.
     */
    @Transactional
    public void seedDemoFinancialsIfEmpty(User owner) {
        if (transactionRepository.countByOwner_Id(owner.getId()) > 0) {
            return;
        }
        seedDemoFinancials(owner);
    }

    /**
     * Removes all financial data for the owner (transactions, budgets, bills, receipts, notifications).
     */
    /** Removes bills created by API/smoke scripts (payee names starting with Smoke test / API probe). */
    @Transactional
    public int purgeSmokeTestBills(User owner) {
        return billRepository.deleteSmokeTestBillsByOwnerId(owner.getId());
    }

    @Transactional
    public void clearSampleData(User owner) {
        receiptStorageService.deleteAllForOwner(owner.getId());
        transactionRepository.deleteByOwner_Id(owner.getId());
        budgetRepository.deleteByOwner_Id(owner.getId());
        billRepository.deleteByOwner_Id(owner.getId());
        notificationRepository.deleteByOwner_Id(owner.getId());
    }

    /**
     * Replaces all transactions and budgets for the owner, then inserts demo data.
     */
    @Transactional
    public void replaceDemoFinancials(User owner) {
        clearSampleData(owner);
        seedDemoFinancials(owner);
    }

    @Transactional
    public void seedDemoFinancials(User owner) {
        YearMonth current = YearMonth.now();
        YearMonth prev1 = current.minusMonths(1);
        YearMonth prev2 = current.minusMonths(2);

        seedMonth(owner, prev2, new BigDecimal("5200.00"));
        seedMonth(owner, prev1, new BigDecimal("5350.00"));
        seedMonth(owner, current, new BigDecimal("5100.00"));

        seedBudgetsForMonth(owner, prev2);
        seedBudgetsForMonth(owner, prev1);
        seedBudgetsForMonth(owner, current);
    }

    private void seedMonth(User owner, YearMonth ym, BigDecimal salaryAmount) {
        LocalDateTime t;

        t = atNoon(ym, 1);
        saveTx(owner, "Salary", salaryAmount, Transaction.TransactionType.INCOME, Transaction.Category.SALARY, t);

        t = atNoon(ym, 2);
        saveTx(owner, "Rent payment", new BigDecimal("1450.00"), Transaction.TransactionType.EXPENSE,
                Transaction.Category.HOUSING_RENT, t);

        t = atNoon(ym, 3);
        saveTx(owner, "Whole Foods", new BigDecimal("112.40"), Transaction.TransactionType.EXPENSE,
                Transaction.Category.FOOD_GROCERIES, t);
        t = atNoon(ym, 7);
        saveTx(owner, "Trader Joe's", new BigDecimal("68.90"), Transaction.TransactionType.EXPENSE,
                Transaction.Category.FOOD_GROCERIES, t);
        t = atNoon(ym, 14);
        saveTx(owner, "Gas station", new BigDecimal("52.00"), Transaction.TransactionType.EXPENSE,
                Transaction.Category.TRANSPORTATION, t);
        t = atNoon(ym, 15);
        saveTx(owner, "Uber to airport", new BigDecimal("38.75"), Transaction.TransactionType.EXPENSE,
                Transaction.Category.TRANSPORTATION, t);
        t = atNoon(ym, 9);
        saveTx(owner, "Dinner — Italian", new BigDecimal("86.20"), Transaction.TransactionType.EXPENSE,
                Transaction.Category.FOOD_RESTAURANT, t);
        t = atNoon(ym, 18);
        saveTx(owner, "Coffee shops", new BigDecimal("24.50"), Transaction.TransactionType.EXPENSE,
                Transaction.Category.FOOD_RESTAURANT, t);
        t = atNoon(ym, 10);
        saveTx(owner, "Electric bill", new BigDecimal("94.00"), Transaction.TransactionType.EXPENSE,
                Transaction.Category.HOUSING_UTILITIES, t);
        t = atNoon(ym, 11);
        saveTx(owner, "Internet", new BigDecimal("59.99"), Transaction.TransactionType.EXPENSE,
                Transaction.Category.HOUSING_UTILITIES, t);
        t = atNoon(ym, 16);
        saveTx(owner, "Pharmacy", new BigDecimal("32.15"), Transaction.TransactionType.EXPENSE,
                Transaction.Category.HEALTHCARE, t);
        t = atNoon(ym, 20);
        saveTx(owner, "Concert tickets", new BigDecimal("120.00"), Transaction.TransactionType.EXPENSE,
                Transaction.Category.ENTERTAINMENT, t);
        t = atNoon(ym, 22);
        saveTx(owner, "Amazon order", new BigDecimal("47.33"), Transaction.TransactionType.EXPENSE,
                Transaction.Category.SHOPPING, t);
        t = atNoon(ym, 25);
        saveTx(owner, "Gym membership", new BigDecimal("49.00"), Transaction.TransactionType.EXPENSE,
                Transaction.Category.HEALTHCARE, t);
        t = atNoon(ym, 28);
        saveTx(owner, "Freelance payout", new BigDecimal("350.00"), Transaction.TransactionType.INCOME,
                Transaction.Category.FREELANCE, t);
    }

    private void seedBudgetsForMonth(User owner, YearMonth ym) {
        LocalDateTime start = ym.atDay(1).atStartOfDay();

        Budget groceries = new Budget();
        groceries.setName("Groceries — " + ym);
        groceries.setAmount(new BigDecimal("450.00"));
        groceries.setCategory(Transaction.Category.FOOD_GROCERIES);
        groceries.setPeriod(Budget.BudgetPeriod.MONTHLY);
        groceries.setStartDate(start);
        groceries.setEndDate(start.plusMonths(1));
        groceries.setSpentAmount(new BigDecimal("310.00"));
        groceries.setRemainingAmount(new BigDecimal("140.00"));
        groceries.setIsActive(true);
        groceries.setOwner(owner);
        budgetRepository.save(groceries);

        Budget dining = new Budget();
        dining.setName("Dining — " + ym);
        dining.setAmount(new BigDecimal("250.00"));
        dining.setCategory(Transaction.Category.FOOD_RESTAURANT);
        dining.setPeriod(Budget.BudgetPeriod.MONTHLY);
        dining.setStartDate(start);
        dining.setEndDate(start.plusMonths(1));
        dining.setSpentAmount(new BigDecimal("265.00"));
        dining.setRemainingAmount(new BigDecimal("-15.00"));
        dining.setIsActive(true);
        dining.setOwner(owner);
        budgetRepository.save(dining);

        Budget transport = new Budget();
        transport.setName("Transport — " + ym);
        transport.setAmount(new BigDecimal("200.00"));
        transport.setCategory(Transaction.Category.TRANSPORTATION);
        transport.setPeriod(Budget.BudgetPeriod.MONTHLY);
        transport.setStartDate(start);
        transport.setEndDate(start.plusMonths(1));
        transport.setSpentAmount(new BigDecimal("142.00"));
        transport.setRemainingAmount(new BigDecimal("58.00"));
        transport.setIsActive(true);
        transport.setOwner(owner);
        budgetRepository.save(transport);
    }

    private static LocalDateTime atNoon(YearMonth ym, int day) {
        int d = Math.min(day, ym.lengthOfMonth());
        return ym.atDay(d).atTime(12, 0);
    }

    private void saveTx(
            User owner,
            String description,
            BigDecimal amount,
            Transaction.TransactionType type,
            Transaction.Category category,
            LocalDateTime when) {
        Transaction tx = new Transaction();
        tx.setDescription(description);
        tx.setAmount(amount);
        tx.setType(type);
        tx.setCategory(category);
        tx.setTransactionDate(when);
        tx.setOwner(owner);
        transactionRepository.save(tx);
    }
}
