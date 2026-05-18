package com.finance.manager.service;

import com.finance.manager.model.AppNotification;

import java.util.List;

public interface AppNotificationService {

    List<AppNotification> list(Long userId);

    long unreadCount(Long userId);

    void markRead(Long userId, Long id);

    void markAllRead(Long userId);

    void sync(Long userId);
}
