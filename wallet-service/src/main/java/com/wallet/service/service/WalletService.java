package com.wallet.service.service;

import com.wallet.service.dto.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface WalletService {

    WalletResponse createWallet(CreateWalletRequest request);

    BalanceResponse getCurrentBalance(UUID walletId);

    BalanceResponse getHistoricalBalance(UUID walletId, Instant timestamp);

    TransactionResponse deposit(UUID walletId, DepositRequest request);

    TransactionResponse withdraw(UUID walletId, WithdrawalRequest request);

    List<TransactionResponse> transfer(TransferRequest request);

    List<TransactionResponse> getTransactionHistory(UUID walletId, int page, int size);
}

