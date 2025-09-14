package com.ada.tech.notification.application;

import com.ada.tech.notification.domain.TransactionEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionEventListener {

    private final NotificationService notificationService;
    private final Counter counter;

    public TransactionEventListener(NotificationService notificationService, MeterRegistry meterRegistry) {
        this.notificationService = notificationService;
        this.counter = meterRegistry.counter("notification.events.processed");
    }

    @RabbitListener(queues = "${app.rabbitmq.queue:transactions.queue}")
    public void onMessage(TransactionEvent event) {
        notificationService.notify(event);
        counter.increment();
    }
}