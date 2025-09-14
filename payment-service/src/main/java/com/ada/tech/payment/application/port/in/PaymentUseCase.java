package com.ada.tech.payment.application.port.in;

import com.ada.tech.payment.domain.Payment;

public interface PaymentUseCase {
    Payment deposit(Payment payment);
    Payment withdraw(Payment payment);
}