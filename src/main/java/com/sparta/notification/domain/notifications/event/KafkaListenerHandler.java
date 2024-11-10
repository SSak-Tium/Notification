package com.sparta.notification.domain.notifications.event;

import com.sparta.notification.domain.notifications.dto.NotificationMessage;
import com.sparta.notification.domain.notifications.entity.Notification;
import com.sparta.notification.domain.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaListenerHandler {

    private final NotificationService notificationService;
    private final SseEmitterHandler sseEmitterHandler;

    @KafkaListener(topics = "#{T(java.util.Collections).singletonList('notifications')}", groupId = "notification-group")
    public void consume(NotificationMessage message) {
        Notification notification = notificationService.saveNotification(message);

        String topic = "notifications-" + message.getUserId();
        String data = "EventType: " + notification.getEventType() + ", Message: " + notification.getMessage();
        sseEmitterHandler.broadcast(topic, data);

        notificationService.changeStatusRead(notification);
    }
}