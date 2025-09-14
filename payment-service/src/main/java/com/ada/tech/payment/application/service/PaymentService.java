package com.ada.tech.payment.application.service;

import com.ada.tech.payment.application.port.in.PaymentUseCase;
import com.ada.tech.payment.application.port.out.ExternalPaymentPort;
import com.ada.tech.payment.application.port.out.PaymentEventPort;
import com.ada.tech.payment.application.port.out.PaymentRepositoryPort;
import com.ada.tech.payment.domain.Payment;
import com.ada.tech.payment.domain.PaymentStatus;
import com.ada.tech.payment.domain.PaymentType;
import com.ada.tech.payment.domain.event.PaymentCompletedEvent;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class PaymentService implements PaymentUseCase {

    private final PaymentRepositoryPort repository;
    private final ExternalPaymentPort externalPayment;
    private final PaymentEventPort eventPort;

    public PaymentService(PaymentRepositoryPort repository,
                          ExternalPaymentPort externalPayment,
                          PaymentEventPort eventPort) {
        this.repository = repository;
        this.externalPayment = externalPayment;
        this.eventPort = eventPort;
    }

    @Override
    public Payment deposit(Payment payment) {
        return process(payment, PaymentType.DEPOSIT);
    }

    @Override
    public Payment withdraw(Payment payment) {
        return process(payment, PaymentType.WITHDRAW);
    }

    private Payment process(Payment payment, PaymentType type) {
        payment.setType(type);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(Instant.now());
        payment = repository.save(payment);

        boolean success = externalPayment.process(payment);

        payment.setStatus(success ? PaymentStatus.COMPLETED : PaymentStatus.FAILED);
        payment = repository.save(payment);

        if (success) {
            PaymentCompletedEvent event = new PaymentCompletedEvent(payment.getId(), payment.getWalletId(), payment.getAmount(), payment.getType());
            eventPort.publish(event);
        }

        return payment;
    }
}