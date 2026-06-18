package com.finance.manager.service;

import com.finance.manager.model.CategoryRule;
import com.finance.manager.model.Transaction;
import com.finance.manager.model.User;
import com.finance.manager.repository.CategoryRuleRepository;
import com.finance.manager.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class CategoryRuleService {

    private static final int MAX_RULES_PER_USER = 50;
    private static final Pattern SAFE_PATTERN = Pattern.compile("^[\\p{L}\\p{N}\\s._#\\-/&']{1,100}$");

    private final CategoryRuleRepository categoryRuleRepository;
    private final UserRepository userRepository;

    public CategoryRuleService(CategoryRuleRepository categoryRuleRepository, UserRepository userRepository) {
        this.categoryRuleRepository = categoryRuleRepository;
        this.userRepository = userRepository;
    }

    public List<CategoryRule> list(Long userId) {
        return categoryRuleRepository.findByOwner_IdOrderByPriorityDescCreatedAtAsc(userId);
    }

    @Transactional
    public CategoryRule create(Long userId, CategoryRule rule) {
        validatePattern(rule.getPattern());
        if (rule.getCategory() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category is required");
        }
        if (categoryRuleRepository.countByOwner_Id(userId) >= MAX_RULES_PER_USER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum category rules reached");
        }
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        rule.setPattern(normalizePattern(rule.getPattern()));
        rule.setOwner(owner);
        return categoryRuleRepository.save(rule);
    }

    @Transactional
    public CategoryRule update(Long userId, Long id, CategoryRule patch) {
        CategoryRule existing = categoryRuleRepository.findById(id)
                .filter(r -> r.getOwner().getId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rule not found"));
        if (patch.getPattern() != null) {
            validatePattern(patch.getPattern());
            existing.setPattern(normalizePattern(patch.getPattern()));
        }
        if (patch.getCategory() != null) {
            existing.setCategory(patch.getCategory());
        }
        existing.setPriority(patch.getPriority());
        return categoryRuleRepository.save(existing);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        CategoryRule existing = categoryRuleRepository.findById(id)
                .filter(r -> r.getOwner().getId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rule not found"));
        categoryRuleRepository.delete(existing);
    }

    public Optional<Transaction.Category> matchCategory(Long userId, String description) {
        if (description == null || description.isBlank()) {
            return Optional.empty();
        }
        String haystack = description.toLowerCase(Locale.ROOT);
        return categoryRuleRepository.findByOwner_IdOrderByPriorityDescCreatedAtAsc(userId).stream()
                .filter(rule -> haystack.contains(rule.getPattern().toLowerCase(Locale.ROOT)))
                .map(CategoryRule::getCategory)
                .findFirst();
    }

    private void validatePattern(String pattern) {
        if (pattern == null || pattern.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pattern is required");
        }
        String trimmed = pattern.trim();
        if (!SAFE_PATTERN.matcher(trimmed).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pattern contains invalid characters");
        }
    }

    private String normalizePattern(String pattern) {
        return pattern.trim();
    }
}
