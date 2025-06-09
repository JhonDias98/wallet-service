package com.wallet.service.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import static com.wallet.service.config.RabbitMQConfig.EXCHANGE;
import static com.wallet.service.config.RabbitMQConfig.ROUTING_KEY;

@Service
@RequiredArgsConstructor
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(NotificationMessage message) {
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, message);
    }
}
