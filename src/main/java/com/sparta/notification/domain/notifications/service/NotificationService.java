package com.sparta.notification.domain.notifications.service;

import com.sparta.notification.domain.notifications.dto.NotificationMessage;
import com.sparta.notification.domain.notifications.entity.Notification;
import com.sparta.notification.domain.notifications.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));

        sendUnreadNotifications(userId, emitter);
        return emitter;
    }

    private void sendUnreadNotifications(Long userId, SseEmitter emitter) {
        List<Notification> notifications = getUnreadNotifications(userId);
        notifications.forEach(notification -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(notification));
                changeStatusRead(notification);
            } catch (IOException e) {
                emitters.remove(userId);
                emitter.completeWithError(e);
            }
        });
    }

    @Transactional
    public void sendNotification(Long userId, NotificationMessage message) {
        Notification notification = new Notification(message.getUserId(), message.getEventType(), message.getMessage());
        notificationRepository.save(notification);

        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(notification));
            } catch (IOException e) {
                emitters.remove(userId);
                emitter.completeWithError(e);
            }
        }
    }

    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findAllByUserIdAndReadStatusFalse(userId);
    }

    @Transactional
    public void changeStatusRead(Notification notification) {
        notification.changeStatusRead();
        notificationRepository.save(notification);
    }

    @Transactional
    public void processNotification(NotificationMessage message) {
        Notification notification = new Notification(
                message.getUserId(),
                message.getEventType(),
                message.getMessage()
        );
        notificationRepository.save(notification);
    }
}