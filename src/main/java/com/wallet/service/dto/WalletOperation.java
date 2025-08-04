package com.wallet.service.dto;

/**
 * Base sealed interface for wallet operations that apply to a single wallet.
 * It is leveraged with pattern matching for switch statements.
 */
public sealed interface WalletOperation permits DepositRequest, WithdrawalRequest {
}

