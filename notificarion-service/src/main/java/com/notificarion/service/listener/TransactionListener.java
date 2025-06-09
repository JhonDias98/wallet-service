package com.notificarion.service.listener;

import com.notificarion.service.dto.NotificationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.notificarion.service.config.RabbitMQConfig.QUEUE;

@Slf4j
@Component
public class TransactionListener {

    @RabbitListener(queues = QUEUE)
    public void handleNotification(NotificationMessage message) {
        log.info("Received notification for user {} - wallet {} : {} {}", message.getUserId(), message.getWalletId(), message.getType(), message.getAmount());
        // Here you could send an email, push notification, etc.
    }
}
