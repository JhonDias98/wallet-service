package com.ada.tech.payment.domain.event;

import com.ada.tech.payment.domain.PaymentType;
import java.io.Serializable;
import java.math.BigDecimal;

public record PaymentCompletedEvent(Long paymentId,
                                    Long walletId,
                                    BigDecimal amount,
                                    PaymentType type) implements Serializable {
}