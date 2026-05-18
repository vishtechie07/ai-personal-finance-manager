package com.finance.manager.repository;

import com.finance.manager.model.Transaction;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public final class TransactionSpecifications {

    private TransactionSpecifications() {}

    public static Specification<Transaction> forOwner(Long ownerId) {
        return (root, query, cb) -> cb.equal(root.get("owner").get("id"), ownerId);
    }

    public static Specification<Transaction> betweenDates(LocalDateTime start, LocalDateTime end) {
        return (root, query, cb) -> cb.between(root.get("transactionDate"), start, end);
    }

    public static Specification<Transaction> descriptionContains(String search) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("description")), "%" + search.toLowerCase() + "%");
    }

    public static Specification<Transaction> hasCategory(Transaction.Category category) {
        return (root, query, cb) -> cb.equal(root.get("category"), category);
    }

    public static Specification<Transaction> hasType(Transaction.TransactionType type) {
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }
}
