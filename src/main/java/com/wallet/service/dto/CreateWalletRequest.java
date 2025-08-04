package com.wallet.service.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request payload for wallet creation using Java record.
 */
public record CreateWalletRequest(
        @NotNull(message = "User ID is required") Long userId
) {
}

