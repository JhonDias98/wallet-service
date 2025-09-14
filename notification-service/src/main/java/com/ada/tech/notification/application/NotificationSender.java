package com.ada.tech.notification.application;

import com.ada.tech.notification.domain.TransactionEvent;

public interface NotificationSender {
    void send(TransactionEvent event);
}