package com.ada.tech.payment.adapter.in.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record PaymentRequest(@NotNull Long walletId,
                             @NotNull @Positive BigDecimal amount) {
}