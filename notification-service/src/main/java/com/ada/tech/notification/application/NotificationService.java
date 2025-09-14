package com.ada.tech.notification.application;

import com.ada.tech.notification.domain.TransactionEvent;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final List<NotificationSender> senders;

    public NotificationService(List<NotificationSender> senders) {
        this.senders = senders;
    }

    public void notify(TransactionEvent event) {
        senders.forEach(sender -> sender.send(event));
    }
}