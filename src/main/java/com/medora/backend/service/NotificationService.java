package com.medora.backend.service;

import com.medora.backend.entity.Notification;
import com.medora.backend.repository.NotificationRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @PostConstruct
    public void initDefaultNotifications() {
        if (notificationRepository.count() == 0) {
            notificationRepository.save(new Notification(
                    "Emergency Department Queue High",
                    "Emergency Department queue has accumulated to 24 waiting patients. Arrival rate +35%.",
                    "WARNING",
                    "ADMIN",
                    "PATIENT_FLOW"
            ));
            notificationRepository.save(new Notification(
                    "ICU Bed Capacity Alert",
                    "ICU bed utilization is approaching capacity (92% occupied, 2 beds available).",
                    "CRITICAL",
                    "ADMIN",
                    "ICU"
            ));
            notificationRepository.save(new Notification(
                    "Appointment Schedule Updated",
                    "Appointment schedule for Dr. Sarah Connor updated successfully.",
                    "INFO",
                    "STAFF",
                    "APPOINTMENT"
            ));
            notificationRepository.save(new Notification(
                    "Queue Update - Triage Status",
                    "Your triage queue position has updated. Estimated wait time is approximately 12 minutes.",
                    "INFO",
                    "PATIENT",
                    "APPOINTMENT"
            ));
        }
    }

    public List<Notification> getNotificationsForUser(String role, Long userId) {
        if (role == null) role = "STAFF";
        return notificationRepository.findNotificationsForUser(role.toUpperCase(), userId);
    }

    public List<Notification> getUnreadNotificationsForUser(String role, Long userId) {
        if (role == null) role = "STAFF";
        return notificationRepository.findUnreadNotificationsForUser(role.toUpperCase(), userId);
    }

    public long getUnreadCount(String role, Long userId) {
        if (role == null) role = "STAFF";
        return notificationRepository.countUnreadForUser(role.toUpperCase(), userId);
    }

    public Optional<Notification> markAsRead(Long id) {
        return notificationRepository.findById(id).map(notification -> {
            notification.setRead(true);
            return notificationRepository.save(notification);
        });
    }

    @Transactional
    public void markAllAsRead(String role, Long userId) {
        if (role == null) role = "STAFF";
        notificationRepository.markAllAsReadForUser(role.toUpperCase(), userId);
    }

    public boolean deleteNotification(Long id) {
        if (notificationRepository.existsById(id)) {
            notificationRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Notification createNotification(String title, String message, String type, String userRole, String relatedModule) {
        Notification notification = new Notification(title, message, type, userRole, relatedModule);
        return notificationRepository.save(notification);
    }

    public Notification createUserNotification(Long userId, String userRole, String title, String message, String type, String relatedModule) {
        Notification notification = new Notification(userId, userRole, title, message, type, relatedModule);
        return notificationRepository.save(notification);
    }
}
