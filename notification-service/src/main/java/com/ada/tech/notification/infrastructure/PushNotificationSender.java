package com.ada.tech.notification.infrastructure;

import com.ada.tech.notification.application.NotificationSender;
import com.ada.tech.notification.domain.TransactionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PushNotificationSender implements NotificationSender {

    @Override
    public void send(TransactionEvent event) {
        log.info("Sending push notification for transaction {} to user {}", event.transactionId(), event.userId());
    }
}