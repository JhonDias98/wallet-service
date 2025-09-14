package com.ada.tech.payment.adapter.out.event;

import com.ada.tech.payment.application.port.out.PaymentEventPort;
import com.ada.tech.payment.domain.event.PaymentCompletedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitPaymentEventPublisher implements PaymentEventPort {

    private final RabbitTemplate rabbitTemplate;

    public RabbitPaymentEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(PaymentCompletedEvent event) {
        rabbitTemplate.convertAndSend(RabbitConfig.PAYMENT_COMPLETED_QUEUE, event);
    }
}