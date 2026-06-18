package com.finance.manager.controller;

import com.finance.manager.dto.NotificationView;
import com.finance.manager.security.AuthPrincipal;
import com.finance.manager.service.AppNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final AppNotificationService notificationService;

    public NotificationController(AppNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationView>> list(@AuthenticationPrincipal AuthPrincipal principal) {
        return ResponseEntity.ok(notificationService.listViews(principal.userId()));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount(@AuthenticationPrincipal AuthPrincipal principal) {
        return ResponseEntity.ok(Map.of("count", notificationService.unreadCount(principal.userId())));
    }

    @PostMapping("/sync")
    public ResponseEntity<Void> sync(@AuthenticationPrincipal AuthPrincipal principal) {
        notificationService.sync(principal.userId());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markRead(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id) {
        notificationService.markRead(principal.userId(), id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal AuthPrincipal principal) {
        notificationService.markAllRead(principal.userId());
        return ResponseEntity.ok().build();
    }
}
