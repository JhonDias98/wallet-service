package com.ada.tech.payment.application.service;

import com.ada.tech.payment.application.port.out.ExternalPaymentPort;
import com.ada.tech.payment.application.port.out.PaymentEventPort;
import com.ada.tech.payment.application.port.out.PaymentRepositoryPort;
import com.ada.tech.payment.domain.Payment;
import com.ada.tech.payment.domain.PaymentStatus;
import com.ada.tech.payment.domain.PaymentType;
import com.ada.tech.payment.domain.event.PaymentCompletedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    PaymentRepositoryPort repository;

    @Mock
    ExternalPaymentPort externalPort;

    @Mock
    PaymentEventPort eventPort;

    @InjectMocks
    PaymentService service;

    @Test
    void depositPublishesEventOnSuccess() {
        Payment payment = new Payment();
        payment.setWalletId(1L);
        payment.setAmount(new BigDecimal("10.00"));
        when(repository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            if (p.getId() == null) p.setId(1L);
            return p;
        });
        when(externalPort.process(any(Payment.class))).thenReturn(true);

        Payment result = service.deposit(payment);

        assertEquals(PaymentStatus.COMPLETED, result.getStatus());
        ArgumentCaptor<PaymentCompletedEvent> captor = ArgumentCaptor.forClass(PaymentCompletedEvent.class);
        verify(eventPort).publish(captor.capture());
        assertEquals(1L, captor.getValue().paymentId());
        assertEquals(PaymentType.DEPOSIT, captor.getValue().type());
    }

    @Test
    void withdrawReturnsFailedWhenProviderFails() {
        Payment payment = new Payment();
        payment.setWalletId(1L);
        payment.setAmount(new BigDecimal("5.00"));
        when(repository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(externalPort.process(any(Payment.class))).thenReturn(false);

        Payment result = service.withdraw(payment);

        assertEquals(PaymentStatus.FAILED, result.getStatus());
        verify(eventPort, never()).publish(any());
    }
}