package com.sparta.notification.domain.notifications.service;

import com.sparta.notification.domain.notifications.dto.NotificationMessage;
import com.sparta.notification.domain.notifications.entity.Notification;
import com.sparta.notification.domain.notifications.event.KafkaListenerHandler;
import com.sparta.notification.domain.notifications.event.SseEmitterHandler;
import com.sparta.notification.domain.notifications.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SseEmitterHandler sseEmitterHandler;
    private final KafkaListenerHandler kafkaListenerHandler;

    public SseEmitter subscribe(Long userId) {
        String topic = "notifications-" + userId;
        SseEmitter sseEmitter = sseEmitterHandler.addEmitter(topic);
        return sseEmitter;
    }

    @Transactional
    public Notification saveNotification(NotificationMessage message) {
        Notification notification = new Notification(message.getUserId(), message.getEventType(), message.getMessage());
        return notificationRepository.save(notification);
    }

    @Transactional
    public void changeStatusRead(Notification notification) {
        notification.changeStatusRead();
        notificationRepository.save(notification);
    }

    // 14일이 지난 읽음 처리된 알림 삭제 스케줄러 (매일 자정 실행)
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void deleteOldNotifications() {
        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(14);
        List<Notification> oldNotifications = notificationRepository.findByReadStatusTrueAndCreatedAtBefore(thresholdDate);
        notificationRepository.deleteAll(oldNotifications);
    }
}