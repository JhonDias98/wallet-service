package com.ada.tech.payment.adapter.out.persistence;

import com.ada.tech.payment.application.port.out.PaymentRepositoryPort;
import com.ada.tech.payment.domain.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentRepositoryAdapter implements PaymentRepositoryPort {

    private final SpringDataPaymentRepository repository;

    public PaymentRepositoryAdapter(SpringDataPaymentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Payment save(Payment payment) {
        return repository.save(payment);
    }
}