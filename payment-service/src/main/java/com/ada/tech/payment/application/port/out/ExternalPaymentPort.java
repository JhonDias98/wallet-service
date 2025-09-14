package com.ada.tech.payment.application.port.out;

import com.ada.tech.payment.domain.Payment;

public interface ExternalPaymentPort {
    boolean process(Payment payment);
}