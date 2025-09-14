package com.ada.tech.payment.adapter.in.controller.dto;

import com.ada.tech.payment.domain.PaymentStatus;
import com.ada.tech.payment.domain.PaymentType;
import java.math.BigDecimal;

public record PaymentResponse(Long id,
                              Long walletId,
                              BigDecimal amount,
                              PaymentType type,
                              PaymentStatus status) {
}