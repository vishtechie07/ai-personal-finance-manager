package com.finance.manager.service.impl;

import com.finance.manager.model.AppNotification;
import com.finance.manager.repository.AppNotificationRepository;
import com.finance.manager.service.AppNotificationService;
import com.finance.manager.service.NotificationSyncService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AppNotificationServiceImpl implements AppNotificationService {

    private final AppNotificationRepository notificationRepository;
    private final NotificationSyncService notificationSyncService;

    public AppNotificationServiceImpl(
            AppNotificationRepository notificationRepository,
            NotificationSyncService notificationSyncService) {
        this.notificationRepository = notificationRepository;
        this.notificationSyncService = notificationSyncService;
    }

    @Override
    @Transactional
    public List<AppNotification> list(Long userId) {
        notificationSyncService.syncForUser(userId);
        return notificationRepository.findByOwner_IdOrderByCreatedAtDesc(userId);
    }

    @Override
    public long unreadCount(Long userId) {
        return notificationRepository.countByOwner_IdAndReadFalse(userId);
    }

    @Override
    @Transactional
    public void markRead(Long userId, Long id) {
        AppNotification n = notificationRepository.findByIdAndOwner_Id(id, userId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        n.setRead(true);
        notificationRepository.save(n);
    }

    @Override
    @Transactional
    public void markAllRead(Long userId) {
        List<AppNotification> all = notificationRepository.findByOwner_IdOrderByCreatedAtDesc(userId);
        for (AppNotification n : all) {
            if (!n.isRead()) {
                n.setRead(true);
            }
        }
        notificationRepository.saveAll(all);
    }

    @Override
    public void sync(Long userId) {
        notificationSyncService.syncForUser(userId);
    }
}
