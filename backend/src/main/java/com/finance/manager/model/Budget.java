package com.finance.manager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Entity
@Table(name = "budgets")
public class Budget {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Budget name is required")
    @Column(nullable = false)
    private String name;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Transaction.Category category;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BudgetPeriod period;
    
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;
    
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;
    
    @Column(name = "spent_amount", precision = 19, scale = 2)
    private BigDecimal spentAmount = BigDecimal.ZERO;
    
    @Column(name = "remaining_amount", precision = 19, scale = 2)
    private BigDecimal remainingAmount;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Default constructor
    public Budget() {}
    
    // Constructor with required fields
    public Budget(String name, BigDecimal amount, Transaction.Category category, BudgetPeriod period, LocalDateTime startDate) {
        this.name = name;
        this.amount = amount;
        this.category = category;
        this.period = period;
        this.startDate = startDate != null ? startDate : LocalDateTime.now();
        this.endDate = calculateEndDate(this.startDate, period);
        this.remainingAmount = amount;
    }
    
    // Helper method to calculate end date based on period
    private LocalDateTime calculateEndDate(LocalDateTime startDate, BudgetPeriod period) {
        switch (period) {
            case WEEKLY:
                return startDate.plusWeeks(1);
            case MONTHLY:
                return startDate.plusMonths(1);
            case QUARTERLY:
                return startDate.plusMonths(3);
            case YEARLY:
                return startDate.plusYears(1);
            default:
                return startDate.plusMonths(1);
        }
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (startDate == null) {
            startDate = LocalDateTime.now();
        }
        if (endDate == null) {
            endDate = calculateEndDate(startDate, period);
        }
        if (remainingAmount == null) {
            remainingAmount = amount;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (remainingAmount == null) {
            remainingAmount = amount.subtract(spentAmount);
        }
    }
    
    // Helper methods
    public void addSpentAmount(BigDecimal amount) {
        this.spentAmount = this.spentAmount.add(amount);
        this.remainingAmount = this.amount.subtract(this.spentAmount);
    }
    
    public BigDecimal getUtilizationPercentage() {
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return spentAmount.divide(amount, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
    
    public boolean isOverBudget() {
        return spentAmount.compareTo(amount) > 0;
    }
    
    public boolean isNearLimit() {
        BigDecimal threshold = amount.multiply(BigDecimal.valueOf(0.8)); // 80% threshold
        return spentAmount.compareTo(threshold) >= 0;
    }
    
    // Get the month this budget belongs to
    public YearMonth getBudgetMonth() {
        return YearMonth.from(startDate);
    }
    
    // Check if budget is for a specific month
    public boolean isForMonth(YearMonth month) {
        return getBudgetMonth().equals(month);
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public Transaction.Category getCategory() {
        return category;
    }
    
    public void setCategory(Transaction.Category category) {
        this.category = category;
    }
    
    public BudgetPeriod getPeriod() {
        return period;
    }
    
    public void setPeriod(BudgetPeriod period) {
        this.period = period;
    }
    
    public LocalDateTime getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
        if (this.endDate == null || this.period != null) {
            this.endDate = calculateEndDate(startDate, this.period);
        }
    }
    
    public LocalDateTime getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
    
    public BigDecimal getSpentAmount() {
        return spentAmount;
    }
    
    public void setSpentAmount(BigDecimal spentAmount) {
        this.spentAmount = spentAmount;
    }
    
    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }
    
    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public enum BudgetPeriod {
        WEEKLY, MONTHLY, QUARTERLY, YEARLY
    }
}
