package com.ada.tech.wallet.service;

import com.ada.tech.wallet.dto.BalanceResponse;
import com.ada.tech.wallet.dto.CreateWalletRequest;
import com.ada.tech.wallet.dto.DepositRequest;
import com.ada.tech.wallet.dto.TransactionResponse;
import com.ada.tech.wallet.dto.TransferRequest;
import com.ada.tech.wallet.dto.WalletResponse;
import com.ada.tech.wallet.dto.WithdrawalRequest;
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

