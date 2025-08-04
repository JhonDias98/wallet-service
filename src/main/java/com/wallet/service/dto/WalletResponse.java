package com.wallet.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WalletResponse(
        UUID id,
        Long userId,
        BigDecimal balance,
        Instant createdAt,
        Instant updatedAt
) {
}

