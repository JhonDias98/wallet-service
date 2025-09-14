package com.ada.tech.payment.application.port.out;

import com.ada.tech.payment.domain.Payment;

public interface PaymentRepositoryPort {
    Payment save(Payment payment);
}