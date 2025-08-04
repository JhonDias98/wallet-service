package com.wallet.service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request object for transfer operations between wallets.
 */
public record TransferRequest(
        @NotNull(message = "Source wallet ID is required") UUID sourceWalletId,
        @NotNull(message = "Destination wallet ID is required") UUID destinationWalletId,
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive") BigDecimal amount,
        String description
) {
}

