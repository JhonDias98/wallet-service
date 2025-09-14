package com.wallet.service.service.impl;

import com.wallet.service.dto.*;
import com.wallet.service.exception.InsufficientFundsException;
import com.wallet.service.exception.WalletAlreadyExistsException;
import com.wallet.service.exception.WalletNotFoundException;
import com.wallet.service.model.Transaction;
import com.wallet.service.model.TransactionStatus;
import com.wallet.service.model.TransactionType;
import com.wallet.service.model.Wallet;
import com.wallet.service.repository.TransactionRepository;
import com.wallet.service.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private WalletServiceImpl walletService;

    private UUID walletId;
    private Wallet wallet;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        walletId = UUID.randomUUID();
        wallet = Wallet.builder()
                .id(walletId)
                .userId(1L)
                .balance(BigDecimal.valueOf(1000))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .walletId(walletId)
                .type(TransactionType.DEPOSIT)
                .amount(BigDecimal.valueOf(500))
                .timestamp(Instant.now())
                .status(TransactionStatus.COMPLETED)
                .balanceAfter(BigDecimal.valueOf(1500))
                .build();
    }

    @Test
    void createWallet_Success() {
        CreateWalletRequest request = new CreateWalletRequest(1L);
        when(walletRepository.existsByUserId(1L)).thenReturn(false);
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        WalletResponse response = walletService.createWallet(request);

        assertNotNull(response);
        assertEquals(walletId, response.getId());
        assertEquals(1L, response.getUserId());
        verify(walletRepository).existsByUserId(1L);
        verify(walletRepository).save(any(Wallet.class));
        verify(walletRepository).flush();
    }

    @Test
    void createWallet_WalletAlreadyExists() {
        CreateWalletRequest request = new CreateWalletRequest(1L);
        when(walletRepository.existsByUserId(1L)).thenReturn(true);

        assertThrows(WalletAlreadyExistsException.class, () -> walletService.createWallet(request));
        verify(walletRepository).existsByUserId(1L);
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void getCurrentBalance_Success() {
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        BalanceResponse response = walletService.getCurrentBalance(walletId);

        assertNotNull(response);
        assertEquals(walletId, response.getWalletId());
        assertEquals(BigDecimal.valueOf(1000), response.getBalance());
        verify(walletRepository).findById(walletId);
    }

    @Test
    void getCurrentBalance_WalletNotFound() {
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class, () -> walletService.getCurrentBalance(walletId));
        verify(walletRepository).findById(walletId);
    }

    @Test
    void getHistoricalBalance_Success() {
        Instant timestamp = Instant.now();
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(transactionRepository.findLatestTransactionBeforeTimestamp(walletId, timestamp))
                .thenReturn(Optional.of(transaction));

        BalanceResponse response = walletService.getHistoricalBalance(walletId, timestamp);

        assertNotNull(response);
        assertEquals(walletId, response.getWalletId());
        assertEquals(BigDecimal.valueOf(1500), response.getBalance());
        verify(walletRepository).findById(walletId);
        verify(transactionRepository).findLatestTransactionBeforeTimestamp(walletId, timestamp);
    }

    @Test
    void getHistoricalBalance_NoTransactions() {
        Instant timestamp = Instant.now();
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(transactionRepository.findLatestTransactionBeforeTimestamp(walletId, timestamp))
                .thenReturn(Optional.empty());

        BalanceResponse response = walletService.getHistoricalBalance(walletId, timestamp);

        assertNotNull(response);
        assertEquals(walletId, response.getWalletId());
        assertEquals(BigDecimal.ZERO, response.getBalance());
        verify(walletRepository).findById(walletId);
        verify(transactionRepository).findLatestTransactionBeforeTimestamp(walletId, timestamp);
    }

    @Test
    void deposit_Success() {
        DepositRequest request = new DepositRequest(BigDecimal.valueOf(500), "Test deposit");
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        TransactionResponse response = walletService.deposit(walletId, request);

        assertNotNull(response);
        assertEquals(transaction.getId(), response.getId());
        assertEquals(TransactionType.DEPOSIT, response.getType());
        assertEquals(BigDecimal.valueOf(500), response.getAmount());
        assertEquals(TransactionStatus.COMPLETED, response.getStatus());
        verify(walletRepository).findById(walletId);
        verify(walletRepository).save(any(Wallet.class));
        verify(transactionRepository).save(any(Transaction.class));
        verify(transactionRepository).flush();
    }

    @Test
    void withdraw_Success() {
        WithdrawalRequest request = new WithdrawalRequest(BigDecimal.valueOf(500), "Test withdrawal");
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        Transaction withdrawalTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .walletId(walletId)
                .type(TransactionType.WITHDRAWAL)
                .amount(BigDecimal.valueOf(500))
                .timestamp(Instant.now())
                .status(TransactionStatus.COMPLETED)
                .balanceAfter(BigDecimal.valueOf(500))
                .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(withdrawalTransaction);

        TransactionResponse response = walletService.withdraw(walletId, request);

        assertNotNull(response);
        assertEquals(withdrawalTransaction.getId(), response.getId());
        assertEquals(TransactionType.WITHDRAWAL, response.getType());
        assertEquals(BigDecimal.valueOf(500), response.getAmount());
        assertEquals(TransactionStatus.COMPLETED, response.getStatus());
        verify(walletRepository).findById(walletId);
        verify(walletRepository).save(any(Wallet.class));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void withdraw_InsufficientFunds() {
        WithdrawalRequest request = new WithdrawalRequest(BigDecimal.valueOf(1500), "Test withdrawal");
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        assertThrows(InsufficientFundsException.class, () -> walletService.withdraw(walletId, request));
        verify(walletRepository).findById(walletId);
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void transfer_Success() {
        UUID destinationWalletId = UUID.randomUUID();
        Wallet destinationWallet = Wallet.builder()
                .id(destinationWalletId)
                .userId(2L)
                .balance(BigDecimal.valueOf(500))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        TransferRequest request = new TransferRequest(
                walletId, destinationWalletId, BigDecimal.valueOf(300), "Test transfer");

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.findById(destinationWalletId)).thenReturn(Optional.of(destinationWallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet).thenReturn(destinationWallet);

        Transaction sourceTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .walletId(walletId)
                .type(TransactionType.TRANSFER_OUT)
                .amount(BigDecimal.valueOf(300))
                .timestamp(Instant.now())
                .status(TransactionStatus.COMPLETED)
                .referenceId(UUID.randomUUID())
                .balanceAfter(BigDecimal.valueOf(700))
                .build();

        Transaction destinationTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .walletId(destinationWalletId)
                .type(TransactionType.TRANSFER_IN)
                .amount(BigDecimal.valueOf(300))
                .timestamp(Instant.now())
                .status(TransactionStatus.COMPLETED)
                .referenceId(sourceTransaction.getReferenceId())
                .balanceAfter(BigDecimal.valueOf(800))
                .build();

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(sourceTransaction)
                .thenReturn(destinationTransaction);

        List<TransactionResponse> responses = walletService.transfer(request);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(TransactionType.TRANSFER_OUT, responses.get(0).getType());
        assertEquals(TransactionType.TRANSFER_IN, responses.get(1).getType());
        verify(walletRepository, times(2)).findById(any(UUID.class));
        verify(walletRepository, times(2)).save(any(Wallet.class));
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }

    @Test
    void transfer_InsufficientFunds() {
        UUID destinationWalletId = UUID.randomUUID();
        Wallet destinationWallet = Wallet.builder()
                .id(destinationWalletId)
                .userId(2L)
                .balance(BigDecimal.valueOf(500))
                .build();

        TransferRequest request = new TransferRequest(
                walletId, destinationWalletId, BigDecimal.valueOf(1500), "Test transfer");

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.findById(destinationWalletId)).thenReturn(Optional.of(destinationWallet));

        assertThrows(InsufficientFundsException.class, () -> walletService.transfer(request));
        verify(walletRepository, times(2)).findById(any(UUID.class));
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void getTransactionHistory_Success() {
        int page = 0;
        int size = 10;
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(transactionRepository.findByWalletIdOrderByTimestampDesc(
                eq(walletId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(transaction)));

        List<TransactionResponse> responses = walletService.getTransactionHistory(walletId, page, size);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(transaction.getId(), responses.get(0).getId());
        verify(walletRepository).findById(walletId);
        verify(transactionRepository).findByWalletIdOrderByTimestampDesc(
                eq(walletId), any(Pageable.class));
    }

}