package com.wallet.service.service;

import com.wallet.service.dto.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface WalletService {

    WalletResponse createWallet(CreateWalletRequest request);

    BalanceResponse getCurrentBalance(UUID walletId);

    BalanceResponse getHistoricalBalance(UUID walletId, Instant timestamp);

    /**
     * Process a deposit or withdrawal using pattern matching for switch.
     */
    TransactionResponse processOperation(UUID walletId, WalletOperation request);

    List<TransactionResponse> transfer(TransferRequest request);

    List<TransactionResponse> getTransactionHistory(UUID walletId, int page, int size);
}

