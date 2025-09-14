package com.ada.tech.payment.application.port.out;

import com.ada.tech.payment.domain.event.PaymentCompletedEvent;

public interface PaymentEventPort {
    void publish(PaymentCompletedEvent event);
}