package com.wallet.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wallet.service.model.TransactionStatus;
import com.wallet.service.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TransactionResponse(
        UUID id,
        UUID walletId,
        TransactionType type,
        BigDecimal amount,
        Instant timestamp,
        TransactionStatus status,
        UUID referenceId,
        String description,
        BigDecimal balanceAfter
) {
}

