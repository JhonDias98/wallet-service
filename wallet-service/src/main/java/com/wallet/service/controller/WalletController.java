package com.wallet.service.controller;

import com.wallet.service.dto.*;
import com.wallet.service.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
@Slf4j
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        log.info("REST request to create wallet for user ID: {}", request.getUserId());
        WalletResponse response = walletService.createWallet(request);
        return ResponseEntity.created(buildURI(response.getId())).body(response);
    }

    @GetMapping("/{walletId}/balance")
    public ResponseEntity<BalanceResponse> getCurrentBalance(@PathVariable UUID walletId) {
        log.info("REST request to get current balance for wallet ID: {}", walletId);
        BalanceResponse response = walletService.getCurrentBalance(walletId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{walletId}/balance/history")
    public ResponseEntity<BalanceResponse> getHistoricalBalance(
            @PathVariable UUID walletId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant timestamp) {
        log.info("REST request to get historical balance for wallet ID: {} at timestamp: {}", walletId, timestamp);
        BalanceResponse response = walletService.getHistoricalBalance(walletId, timestamp);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{walletId}/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            @PathVariable UUID walletId,
            @Valid @RequestBody DepositRequest request) {
        log.info("REST request to deposit {} to wallet ID: {}", request.getAmount(), walletId);
        TransactionResponse response = walletService.deposit(walletId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{walletId}/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            @PathVariable UUID walletId,
            @Valid @RequestBody WithdrawalRequest request) {
        log.info("REST request to withdraw {} from wallet ID: {}", request.getAmount(), walletId);
        TransactionResponse response = walletService.withdraw(walletId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<List<TransactionResponse>> transfer(@Valid @RequestBody TransferRequest request) {
        log.info("REST request to transfer {} from wallet ID: {} to wallet ID: {}",
                request.getAmount(), request.getSourceWalletId(), request.getDestinationWalletId());
        List<TransactionResponse> responses = walletService.transfer(request);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{walletId}/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactionHistory(
            @PathVariable UUID walletId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("REST request to get transaction history for wallet ID: {}, page: {}, size: {}", walletId, page, size);
        List<TransactionResponse> responses = walletService.getTransactionHistory(walletId, page, size);
        return ResponseEntity.ok(responses);
    }

    private URI buildURI(UUID id) {
        return ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
    }

}

