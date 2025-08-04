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
import com.wallet.service.service.NotificationService;
import com.wallet.service.service.WalletService;
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
    private final NotificationService notificationService;

    @Override
    @Transactional
    public WalletResponse createWallet(CreateWalletRequest request) {
        log.info("Creating wallet for user ID: {}", request.userId());

        if (walletRepository.existsByUserId(request.userId())) {
            throw new WalletAlreadyExistsException("Wallet already exists for user ID: " + request.userId());
        }

        Wallet wallet = Wallet.builder()
                .userId(request.userId())
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

        return new BalanceResponse(
                wallet.getId(),
                wallet.getBalance(),
                Instant.now()
        );
    }

    @Override
    public BalanceResponse getHistoricalBalance(UUID walletId, Instant timestamp) {
        log.info("Getting historical balance for wallet ID: {} at timestamp: {}", walletId, timestamp);
        
        getWalletById(walletId);

        Optional<Transaction> latestTransaction = transactionRepository
                .findLatestTransactionBeforeTimestamp(walletId, timestamp);

        if (latestTransaction.isPresent()) {
            return new BalanceResponse(
                    walletId,
                    latestTransaction.get().getBalanceAfter(),
                    timestamp
            );
        } else {
            return new BalanceResponse(
                    walletId,
                    BigDecimal.ZERO,
                    timestamp
            );
        }
    }

    @Override
    @Transactional
    public TransactionResponse processOperation(UUID walletId, WalletOperation request) {
        return switch (request) {
            case DepositRequest deposit -> deposit(walletId, deposit);
            case WithdrawalRequest withdrawal -> withdraw(walletId, withdrawal);
        };
    }

    private TransactionResponse deposit(UUID walletId, DepositRequest request) {
        log.info("Depositing {} to wallet ID: {}", request.amount(), walletId);

        Wallet wallet = getWalletById(walletId);

        Transaction transaction = Transaction.builder()
                .walletId(walletId)
                .type(TransactionType.DEPOSIT)
                .amount(request.amount())
                .status(TransactionStatus.PENDING)
                .description(request.description())
                .build();

        try {
            wallet.setBalance(wallet.getBalance().add(request.amount()));
            walletRepository.save(wallet);

            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setBalanceAfter(wallet.getBalance());
            Transaction savedTransaction = transactionRepository.save(transaction);
            transactionRepository.flush();

            log.info("Deposit completed successfully. Transaction ID: {}", savedTransaction.getId());
            return mapToTransactionResponse(savedTransaction);
        } catch (Exception e) {
            log.error("Deposit failed", e);
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw new TransactionFailedException("Deposit failed: " + e.getMessage(), e);
        }
    }

    private TransactionResponse withdraw(UUID walletId, WithdrawalRequest request) {
        log.info("Withdrawing {} from wallet ID: {}", request.amount(), walletId);

        Wallet wallet = getWalletById(walletId);

        if (wallet.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds in wallet ID: " + walletId);
        }

        Transaction transaction = Transaction.builder()
                .walletId(walletId)
                .type(TransactionType.WITHDRAWAL)
                .amount(request.amount())
                .status(TransactionStatus.PENDING)
                .description(request.description())
                .build();

        try {
            wallet.setBalance(wallet.getBalance().subtract(request.amount()));
            walletRepository.save(wallet);

            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setBalanceAfter(wallet.getBalance());
            Transaction savedTransaction = transactionRepository.save(transaction);
            transactionRepository.flush();

            log.info("Withdrawal completed successfully. Transaction ID: {}", savedTransaction.getId());
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
                request.amount(), request.sourceWalletId(), request.destinationWalletId());

        Wallet sourceWallet = getWalletById(request.sourceWalletId());
        Wallet destinationWallet = getWalletById(request.destinationWalletId());

        if (sourceWallet.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds in source wallet ID: " + request.sourceWalletId());
        }

        UUID referenceId = UUID.randomUUID();

        Transaction sourceTransaction = Transaction.builder()
                .walletId(request.sourceWalletId())
                .type(TransactionType.TRANSFER_OUT)
                .amount(request.amount())
                .status(TransactionStatus.PENDING)
                .referenceId(referenceId)
                .description(request.description())
                .build();

        Transaction destinationTransaction = Transaction.builder()
                .walletId(request.destinationWalletId())
                .type(TransactionType.TRANSFER_IN)
                .amount(request.amount())
                .status(TransactionStatus.PENDING)
                .referenceId(referenceId)
                .description(request.description())
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
            notificationService.sendTransferNotification(request);

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
        return new WalletResponse(
                wallet.getId(),
                wallet.getUserId(),
                wallet.getBalance(),
                wallet.getCreatedAt(),
                wallet.getUpdatedAt()
        );
    }

    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getWalletId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getTimestamp(),
                transaction.getStatus(),
                transaction.getReferenceId(),
                transaction.getDescription(),
                transaction.getBalanceAfter()
        );
    }

}

