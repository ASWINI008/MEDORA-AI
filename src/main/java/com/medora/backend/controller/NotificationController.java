package com.medora.backend.controller;

import com.medora.backend.entity.Notification;
import com.medora.backend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(
            @RequestParam(required = false, defaultValue = "STAFF") String role,
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(notificationService.getNotificationsForUser(role, userId));
    }

    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(
            @RequestParam(required = false, defaultValue = "STAFF") String role,
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadNotificationsForUser(role, userId));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @RequestParam(required = false, defaultValue = "STAFF") String role,
            @RequestParam(required = false) Long userId) {
        long count = notificationService.getUnreadCount(role, userId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        return notificationService.markAsRead(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(
            @RequestParam(required = false, defaultValue = "STAFF") String role,
            @RequestParam(required = false) Long userId) {
        notificationService.markAllAsRead(role, userId);
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        boolean deleted = notificationService.deleteNotification(id);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Notification deleted successfully"));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Notification> createNotification(@RequestBody Map<String, Object> body) {
        String title = (String) body.getOrDefault("title", "System Notification");
        String message = (String) body.getOrDefault("message", "Operational update");
        String type = (String) body.getOrDefault("type", "INFO");
        String role = (String) body.getOrDefault("userRole", "ADMIN");
        String module = (String) body.getOrDefault("relatedModule", "SYSTEM");

        Notification notification = notificationService.createNotification(title, message, type, role, module);
        return ResponseEntity.ok(notification);
    }
}
