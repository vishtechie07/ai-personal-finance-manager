package com.finance.manager.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(unique = true, nullable = false)
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(unique = true, nullable = false)
    private String email;
    
    @JsonIgnore
    @Column(nullable = true)
    private String password;

    @Column(name = "google_sub", unique = true)
    private String googleSub;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false)
    private AuthProvider authProvider = AuthProvider.LOCAL;
    
    @Column(name = "first_name")
    private String firstName;
    
    @Column(name = "last_name")
    private String lastName;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Enumerated(EnumType.STRING)
    private Role role;

    /**
     * AES-GCM encrypted OpenAI API key (never exposed in JSON).
     */
    @Column(name = "openai_api_key_encrypted", columnDefinition = "TEXT")
    private String openaiApiKeyEncrypted;

    @Column(name = "bill_reminders_enabled", nullable = false)
    private boolean billRemindersEnabled = true;

    @Column(name = "bill_reminder_days_before", nullable = false)
    private int billReminderDaysBefore = 3;

    @Column(name = "last_bill_reminder_sent_at")
    private LocalDateTime lastBillReminderSentAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (role == null) {
            role = Role.USER;
        }
        if (authProvider == null) {
            authProvider = AuthProvider.LOCAL;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
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
    
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }

    @JsonIgnore
    public String getOpenaiApiKeyEncrypted() {
        return openaiApiKeyEncrypted;
    }

    public void setOpenaiApiKeyEncrypted(String openaiApiKeyEncrypted) {
        this.openaiApiKeyEncrypted = openaiApiKeyEncrypted;
    }
    
    public String getGoogleSub() {
        return googleSub;
    }

    public void setGoogleSub(String googleSub) {
        this.googleSub = googleSub;
    }

    public AuthProvider getAuthProvider() {
        return authProvider;
    }

    public void setAuthProvider(AuthProvider authProvider) {
        this.authProvider = authProvider;
    }

    public boolean isBillRemindersEnabled() {
        return billRemindersEnabled;
    }

    public void setBillRemindersEnabled(boolean billRemindersEnabled) {
        this.billRemindersEnabled = billRemindersEnabled;
    }

    public int getBillReminderDaysBefore() {
        return billReminderDaysBefore;
    }

    public void setBillReminderDaysBefore(int billReminderDaysBefore) {
        this.billReminderDaysBefore = Math.max(1, Math.min(14, billReminderDaysBefore));
    }

    public LocalDateTime getLastBillReminderSentAt() {
        return lastBillReminderSentAt;
    }

    public void setLastBillReminderSentAt(LocalDateTime lastBillReminderSentAt) {
        this.lastBillReminderSentAt = lastBillReminderSentAt;
    }

    public enum AuthProvider {
        LOCAL, GOOGLE
    }

    public enum Role {
        USER, ADMIN
    }
}
