package com.wallet.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BalanceResponse(
        UUID walletId,
        BigDecimal balance,
        Instant timestamp
) {
}

