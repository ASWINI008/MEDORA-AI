package com.medora.backend.repository;

import com.medora.backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByOrderByCreatedAtDesc();

    @Query("SELECT n FROM Notification n WHERE (n.userRole = :role OR n.userRole = 'ALL' OR n.userRole IS NULL OR (n.userId = :userId AND :userId IS NOT NULL)) ORDER BY n.createdAt DESC")
    List<Notification> findNotificationsForUser(@Param("role") String role, @Param("userId") Long userId);

    @Query("SELECT n FROM Notification n WHERE n.isRead = false AND (n.userRole = :role OR n.userRole = 'ALL' OR n.userRole IS NULL OR (n.userId = :userId AND :userId IS NOT NULL)) ORDER BY n.createdAt DESC")
    List<Notification> findUnreadNotificationsForUser(@Param("role") String role, @Param("userId") Long userId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.isRead = false AND (n.userRole = :role OR n.userRole = 'ALL' OR n.userRole IS NULL OR (n.userId = :userId AND :userId IS NOT NULL))")
    long countUnreadForUser(@Param("role") String role, @Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE (n.userRole = :role OR n.userRole = 'ALL' OR n.userRole IS NULL OR (n.userId = :userId AND :userId IS NOT NULL))")
    void markAllAsReadForUser(@Param("role") String role, @Param("userId") Long userId);
}
