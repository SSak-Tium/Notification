package com.sparta.notification.domain.notifications.service;


import com.sparta.notification.domain.notifications.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "notifications", groupId = "notification-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(@Payload NotificationMessage message) {
        notificationService.processNotification(message);
    }
}