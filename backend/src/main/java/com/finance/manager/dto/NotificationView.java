package com.finance.manager.dto;

import com.finance.manager.model.AppNotification;

import java.time.LocalDateTime;

public record NotificationView(
        Long id,
        AppNotification.NotificationType type,
        String title,
        String message,
        boolean read,
        String referenceType,
        Long referenceId,
        String actionPath,
        LocalDateTime createdAt
) {
    public static NotificationView from(AppNotification n) {
        return new NotificationView(
                n.getId(),
                n.getType(),
                n.getTitle(),
                n.getMessage(),
                n.isRead(),
                n.getReferenceType(),
                n.getReferenceId(),
                resolveActionPath(n),
                n.getCreatedAt());
    }

    private static String resolveActionPath(AppNotification n) {
        if (n.getType() == null) {
            return "/dashboard";
        }
        return switch (n.getType()) {
            case BUDGET_WARNING -> "/budgets";
            case BILL_DUE -> "/bills";
            case SPENDING_ALERT -> "/transactions";
        };
    }
}
