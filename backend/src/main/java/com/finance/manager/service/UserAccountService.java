package com.finance.manager.service;

import com.finance.manager.model.Transaction;
import com.finance.manager.model.User;
import com.finance.manager.repository.CategoryRuleRepository;
import com.finance.manager.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class UserAccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DemoSeedService demoSeedService;
    private final CategoryRuleRepository categoryRuleRepository;

    public UserAccountService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            DemoSeedService demoSeedService,
            CategoryRuleRepository categoryRuleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.demoSeedService = demoSeedService;
        this.categoryRuleRepository = categoryRuleRepository;
    }

    @Transactional
    public User updateProfile(Long userId, String firstName, String lastName, String email) {
        User user = requireUser(userId);
        if (email != null && !email.isBlank()) {
            String normalized = email.trim().toLowerCase();
            userRepository.findByEmail(normalized)
                    .filter(u -> !Objects.equals(u.getId(), userId))
                    .ifPresent(u -> {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
                    });
            user.setEmail(normalized);
        }
        if (firstName != null) {
            user.setFirstName(firstName.trim());
        }
        if (lastName != null) {
            user.setLastName(lastName.trim());
        }
        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = requireUser(userId);
        if (user.getAuthProvider() == User.AuthProvider.GOOGLE && user.getPassword() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Google accounts cannot set a password here");
        }
        if (currentPassword == null || newPassword == null || newPassword.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid password");
        }
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void updatePreferences(Long userId, Boolean billRemindersEnabled, Integer billReminderDaysBefore) {
        User user = requireUser(userId);
        if (billRemindersEnabled != null) {
            user.setBillRemindersEnabled(billRemindersEnabled);
        }
        if (billReminderDaysBefore != null) {
            user.setBillReminderDaysBefore(billReminderDaysBefore);
        }
        userRepository.save(user);
    }

    @Transactional
    public void deleteAccount(Long userId, String passwordConfirmation) {
        User user = requireUser(userId);
        if (user.getAuthProvider() == User.AuthProvider.LOCAL) {
            if (passwordConfirmation == null || passwordConfirmation.isBlank()
                    || !passwordEncoder.matches(passwordConfirmation, user.getPassword())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Password confirmation required");
            }
        }
        demoSeedService.clearSampleData(user);
        categoryRuleRepository.deleteByOwner_Id(userId);
        userRepository.delete(user);
    }

    public Map<String, Object> toUserInfo(User user) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", user.getId());
        info.put("username", user.getUsername());
        info.put("email", user.getEmail());
        info.put("firstName", user.getFirstName());
        info.put("lastName", user.getLastName());
        info.put("role", user.getRole());
        info.put("authProvider", user.getAuthProvider());
        info.put("createdAt", user.getCreatedAt());
        info.put("billRemindersEnabled", user.isBillRemindersEnabled());
        info.put("billReminderDaysBefore", user.getBillReminderDaysBefore());
        return info;
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
