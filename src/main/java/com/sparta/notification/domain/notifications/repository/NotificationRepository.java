package com.sparta.notification.domain.notifications.repository;

import com.sparta.notification.domain.notifications.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserId(Long userId);

    List<Notification> findAllByUserIdAndReadStatusFalse(Long userId);
}
