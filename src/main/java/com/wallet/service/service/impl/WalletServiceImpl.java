package com.wallet.service.service.impl;

import com.wallet.service.dto.*;
import com.wallet.service.exception.InsufficientFundsException;
import com.wallet.service.exception.TransactionFailedException;
import com.wallet.service.exception.WalletAlreadyExistsException;
import com.wallet.service.exception.WalletNotFoundException;
import com.wallet.service.model.Transaction;
import com.wallet.service.model.TransactionStatus;
import com.wallet.service.model.TransactionType;
import com.wallet.service.model.Wallet;
import com.wallet.service.repository.TransactionRepository;
import com.wallet.service.repository.WalletRepository;
import com.wallet.service.service.WalletService;
import com.wallet.service.messaging.NotificationMessage;
import com.wallet.service.messaging.NotificationPublisher;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationPublisher notificationPublisher;

    @Override
    @Transactional
    public WalletResponse createWallet(CreateWalletRequest request) {
        log.info("Creating wallet for user ID: {}", request.getUserId());

        if (walletRepository.existsByUserId(request.getUserId())) {
            throw new WalletAlreadyExistsException("Wallet already exists for user ID: " + request.getUserId());
        }
        
        Wallet wallet = Wallet.builder()
                .userId(request.getUserId())
                .balance(BigDecimal.ZERO)
                .build();

        Wallet savedWallet = walletRepository.save(wallet);
        walletRepository.flush();

        log.info("Wallet created successfully with ID: {}", savedWallet.getId());
        
        return mapToWalletResponse(savedWallet);
    }

    @Override
    public BalanceResponse getCurrentBalance(UUID walletId) {
        log.info("Getting current balance for wallet ID: {}", walletId);
        
        Wallet wallet = getWalletById(walletId);
        
        return BalanceResponse.builder()
                .walletId(wallet.getId())
                .balance(wallet.getBalance())
                .timestamp(Instant.now())
                .build();
    }

    @Override
    public BalanceResponse getHistoricalBalance(UUID walletId, Instant timestamp) {
        log.info("Getting historical balance for wallet ID: {} at timestamp: {}", walletId, timestamp);
        
        getWalletById(walletId);

        Optional<Transaction> latestTransaction = transactionRepository.findLatestTransactionBeforeTimestamp(walletId, timestamp);
        
        if (latestTransaction.isPresent()) {
            return BalanceResponse.builder()
                    .walletId(walletId)
                    .balance(latestTransaction.get().getBalanceAfter())
                    .timestamp(timestamp)
                    .build();
        } else {
            return BalanceResponse.builder()
                    .walletId(walletId)
                    .balance(BigDecimal.ZERO)
                    .timestamp(timestamp)
                    .build();
        }
    }

    @Override
    @Transactional
    public TransactionResponse deposit(UUID walletId, DepositRequest request) {
        log.info("Depositing {} to wallet ID: {}", request.getAmount(), walletId);
        
        Wallet wallet = getWalletById(walletId);

        Transaction transaction = Transaction.builder()
                .walletId(walletId)
                .type(TransactionType.DEPOSIT)
                .amount(request.getAmount())
                .status(TransactionStatus.PENDING)
                .description(request.getDescription())
                .build();
        
        try {
            wallet.setBalance(wallet.getBalance().add(request.getAmount()));
            walletRepository.save(wallet);

            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setBalanceAfter(wallet.getBalance());
            Transaction savedTransaction = transactionRepository.save(transaction);
            transactionRepository.flush();

            log.info("Deposit completed successfully. Transaction ID: {}", savedTransaction.getId());

            notificationPublisher.publish(new NotificationMessage(
                    wallet.getUserId(),
                    wallet.getId(),
                    TransactionType.DEPOSIT.name(),
                    request.getAmount(),
                    savedTransaction.getReferenceId(),
                    request.getDescription()));

            return mapToTransactionResponse(savedTransaction);
        } catch (Exception e) {
            log.error("Deposit failed", e);
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw new TransactionFailedException("Deposit failed: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public TransactionResponse withdraw(UUID walletId, WithdrawalRequest request) {
        log.info("Withdrawing {} from wallet ID: {}", request.getAmount(), walletId);
        
        Wallet wallet = getWalletById(walletId);

        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds in wallet ID: " + walletId);
        }

        Transaction transaction = Transaction.builder()
                .walletId(walletId)
                .type(TransactionType.WITHDRAWAL)
                .amount(request.getAmount())
                .status(TransactionStatus.PENDING)
                .description(request.getDescription())
                .build();
        
        try {
            wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
            walletRepository.save(wallet);

            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setBalanceAfter(wallet.getBalance());
            Transaction savedTransaction = transactionRepository.save(transaction);
            transactionRepository.flush();
            
            log.info("Withdrawal completed successfully. Transaction ID: {}", savedTransaction.getId());

            notificationPublisher.publish(new NotificationMessage(
                    wallet.getUserId(),
                    wallet.getId(),
                    TransactionType.WITHDRAWAL.name(),
                    request.getAmount(),
                    savedTransaction.getReferenceId(),
                    request.getDescription()));

            return mapToTransactionResponse(savedTransaction);
        } catch (Exception e) {
            log.error("Withdrawal failed", e);
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw new TransactionFailedException("Withdrawal failed: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public List<TransactionResponse> transfer(TransferRequest request) {
        log.info("Transferring {} from wallet ID: {} to wallet ID: {}", 
                request.getAmount(), request.getSourceWalletId(), request.getDestinationWalletId());

        Wallet sourceWallet = getWalletById(request.getSourceWalletId());
        Wallet destinationWallet = getWalletById(request.getDestinationWalletId());

        if (sourceWallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds in source wallet ID: " + request.getSourceWalletId());
        }

        UUID referenceId = UUID.randomUUID();

        Transaction sourceTransaction = Transaction.builder()
                .walletId(request.getSourceWalletId())
                .type(TransactionType.TRANSFER_OUT)
                .amount(request.getAmount())
                .status(TransactionStatus.PENDING)
                .referenceId(referenceId)
                .description(request.getDescription())
                .build();

        Transaction destinationTransaction = Transaction.builder()
                .walletId(request.getDestinationWalletId())
                .type(TransactionType.TRANSFER_IN)
                .amount(request.getAmount())
                .status(TransactionStatus.PENDING)
                .referenceId(referenceId)
                .description(request.getDescription())
                .build();
        
        try {
            sourceWallet.setBalance(sourceWallet.getBalance().subtract(request.getAmount()));
            walletRepository.save(sourceWallet);

            destinationWallet.setBalance(destinationWallet.getBalance().add(request.getAmount()));
            walletRepository.save(destinationWallet);

            sourceTransaction.setStatus(TransactionStatus.COMPLETED);
            sourceTransaction.setBalanceAfter(sourceWallet.getBalance());
            Transaction savedSourceTransaction = transactionRepository.save(sourceTransaction);
            
            destinationTransaction.setStatus(TransactionStatus.COMPLETED);
            destinationTransaction.setBalanceAfter(destinationWallet.getBalance());
            Transaction savedDestinationTransaction = transactionRepository.save(destinationTransaction);
            
            log.info("Transfer completed successfully. Reference ID: {}", referenceId);

            notificationPublisher.publish(new NotificationMessage(
                    sourceWallet.getUserId(),
                    sourceWallet.getId(),
                    TransactionType.TRANSFER_OUT.name(),
                    request.getAmount(),
                    referenceId,
                    request.getDescription()));

            notificationPublisher.publish(new NotificationMessage(
                    destinationWallet.getUserId(),
                    destinationWallet.getId(),
                    TransactionType.TRANSFER_IN.name(),
                    request.getAmount(),
                    referenceId,
                    request.getDescription()));

            List<TransactionResponse> responses = new ArrayList<>();
            responses.add(mapToTransactionResponse(savedSourceTransaction));
            responses.add(mapToTransactionResponse(savedDestinationTransaction));
            return responses;
        } catch (Exception e) {
            log.error("Transfer failed", e);
            sourceTransaction.setStatus(TransactionStatus.FAILED);
            destinationTransaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(sourceTransaction);
            transactionRepository.save(destinationTransaction);
            throw new TransactionFailedException("Transfer failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<TransactionResponse> getTransactionHistory(UUID walletId, int page, int size) {
        log.info("Getting transaction history for wallet ID: {}, page: {}, size: {}", walletId, page, size);

        getWalletById(walletId);
        
        Page<Transaction> transactions = transactionRepository.findByWalletIdOrderByTimestampDesc(
                walletId, PageRequest.of(page, size, Sort.by("timestamp").descending()));
        
        return transactions.getContent().stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }
    
    private Wallet getWalletById(UUID walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found with ID: " + walletId));
    }
    
    private WalletResponse mapToWalletResponse(Wallet wallet) {
        return WalletResponse.builder()
                .id(wallet.getId())
                .userId(wallet.getUserId())
                .balance(wallet.getBalance())
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }
    
    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .walletId(transaction.getWalletId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .timestamp(transaction.getTimestamp())
                .status(transaction.getStatus())
                .referenceId(transaction.getReferenceId())
                .description(transaction.getDescription())
                .balanceAfter(transaction.getBalanceAfter())
                .build();
    }

}

