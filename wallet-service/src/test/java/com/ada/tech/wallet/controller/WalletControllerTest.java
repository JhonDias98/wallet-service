package com.ada.tech.wallet.controller;

import com.ada.tech.wallet.dto.BalanceResponse;
import com.ada.tech.wallet.dto.CreateWalletRequest;
import com.ada.tech.wallet.dto.DepositRequest;
import com.ada.tech.wallet.dto.TransactionResponse;
import com.ada.tech.wallet.dto.TransferRequest;
import com.ada.tech.wallet.dto.WalletResponse;
import com.ada.tech.wallet.dto.WithdrawalRequest;
import com.ada.tech.wallet.model.TransactionStatus;
import com.ada.tech.wallet.model.TransactionType;
import com.ada.tech.wallet.service.WalletService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ada.tech.wallet.exception.InsufficientFundsException;
import com.ada.tech.wallet.exception.WalletNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WalletController.class)
public class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WalletService walletService;

    @Test
    void createWallet_Success() throws Exception {
        CreateWalletRequest request = new CreateWalletRequest(1L);
        UUID walletId = UUID.randomUUID();
        WalletResponse response = WalletResponse.builder()
                .id(walletId)
                .userId(1L)
                .balance(BigDecimal.ZERO)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(walletService.createWallet(any(CreateWalletRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(walletId.toString())))
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(jsonPath("$.balance", is(0)));
    }

    @Test
    void getCurrentBalance_Success() throws Exception {
        UUID walletId = UUID.randomUUID();
        BalanceResponse response = BalanceResponse.builder()
                .walletId(walletId)
                .balance(BigDecimal.valueOf(1000))
                .timestamp(Instant.now())
                .build();

        when(walletService.getCurrentBalance(walletId)).thenReturn(response);

        mockMvc.perform(get("/api/wallets/{walletId}/balance", walletId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.walletId", is(walletId.toString())))
                .andExpect(jsonPath("$.balance", is(1000)));
    }

    @Test
    void getCurrentBalance_WalletNotFound() throws Exception {
        UUID walletId = UUID.randomUUID();
        when(walletService.getCurrentBalance(walletId))
                .thenThrow(new WalletNotFoundException("Wallet not found with ID: " + walletId));

        mockMvc.perform(get("/api/wallets/{walletId}/balance", walletId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Wallet not found with ID: " + walletId)));
    }

    @Test
    void deposit_Success() throws Exception {
        UUID walletId = UUID.randomUUID();
        DepositRequest request = new DepositRequest(BigDecimal.valueOf(500), "Test deposit");
        TransactionResponse response = TransactionResponse.builder()
                .id(UUID.randomUUID())
                .walletId(walletId)
                .type(TransactionType.DEPOSIT)
                .amount(BigDecimal.valueOf(500))
                .timestamp(Instant.now())
                .status(TransactionStatus.COMPLETED)
                .description("Test deposit")
                .balanceAfter(BigDecimal.valueOf(1500))
                .build();

        when(walletService.deposit(eq(walletId), any(DepositRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/wallets/{walletId}/deposit", walletId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.walletId", is(walletId.toString())))
                .andExpect(jsonPath("$.type", is("DEPOSIT")))
                .andExpect(jsonPath("$.amount", is(500)))
                .andExpect(jsonPath("$.status", is("COMPLETED")))
                .andExpect(jsonPath("$.description", is("Test deposit")))
                .andExpect(jsonPath("$.balanceAfter", is(1500)));
    }

    @Test
    void withdraw_Success() throws Exception {
        UUID walletId = UUID.randomUUID();
        WithdrawalRequest request = new WithdrawalRequest(BigDecimal.valueOf(500), "Test withdrawal");
        TransactionResponse response = TransactionResponse.builder()
                .id(UUID.randomUUID())
                .walletId(walletId)
                .type(TransactionType.WITHDRAWAL)
                .amount(BigDecimal.valueOf(500))
                .timestamp(Instant.now())
                .status(TransactionStatus.COMPLETED)
                .description("Test withdrawal")
                .balanceAfter(BigDecimal.valueOf(500))
                .build();

        when(walletService.withdraw(eq(walletId), any(WithdrawalRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/wallets/{walletId}/withdraw", walletId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.walletId", is(walletId.toString())))
                .andExpect(jsonPath("$.type", is("WITHDRAWAL")))
                .andExpect(jsonPath("$.amount", is(500)))
                .andExpect(jsonPath("$.status", is("COMPLETED")))
                .andExpect(jsonPath("$.description", is("Test withdrawal")))
                .andExpect(jsonPath("$.balanceAfter", is(500)));
    }

    @Test
    void withdraw_InsufficientFunds() throws Exception {
        UUID walletId = UUID.randomUUID();
        WithdrawalRequest request = new WithdrawalRequest(BigDecimal.valueOf(1500), "Test withdrawal");
        when(walletService.withdraw(eq(walletId), any(WithdrawalRequest.class)))
                .thenThrow(new InsufficientFundsException("Insufficient funds in wallet ID: " + walletId));

        mockMvc.perform(post("/api/wallets/{walletId}/withdraw", walletId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Insufficient funds in wallet ID: " + walletId)));
    }

    @Test
    void transfer_Success() throws Exception {
        UUID sourceWalletId = UUID.randomUUID();
        UUID destinationWalletId = UUID.randomUUID();
        TransferRequest request = new TransferRequest(
                sourceWalletId, destinationWalletId, BigDecimal.valueOf(300), "Test transfer");

        TransactionResponse sourceResponse = TransactionResponse.builder()
                .id(UUID.randomUUID())
                .walletId(sourceWalletId)
                .type(TransactionType.TRANSFER_OUT)
                .amount(BigDecimal.valueOf(300))
                .timestamp(Instant.now())
                .status(TransactionStatus.COMPLETED)
                .description("Test transfer")
                .balanceAfter(BigDecimal.valueOf(700))
                .build();

        TransactionResponse destinationResponse = TransactionResponse.builder()
                .id(UUID.randomUUID())
                .walletId(destinationWalletId)
                .type(TransactionType.TRANSFER_IN)
                .amount(BigDecimal.valueOf(300))
                .timestamp(Instant.now())
                .status(TransactionStatus.COMPLETED)
                .description("Test transfer")
                .balanceAfter(BigDecimal.valueOf(800))
                .build();

        List<TransactionResponse> responses = Arrays.asList(sourceResponse, destinationResponse);
        when(walletService.transfer(any(TransferRequest.class))).thenReturn(responses);

        mockMvc.perform(post("/api/wallets/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].walletId", is(sourceWalletId.toString())))
                .andExpect(jsonPath("$[0].type", is("TRANSFER_OUT")))
                .andExpect(jsonPath("$[0].amount", is(300)))
                .andExpect(jsonPath("$[1].walletId", is(destinationWalletId.toString())))
                .andExpect(jsonPath("$[1].type", is("TRANSFER_IN")))
                .andExpect(jsonPath("$[1].amount", is(300)));
    }

    @Test
    void getTransactionHistory_Success() throws Exception {
        UUID walletId = UUID.randomUUID();
        TransactionResponse transaction = TransactionResponse.builder()
                .id(UUID.randomUUID())
                .walletId(walletId)
                .type(TransactionType.DEPOSIT)
                .amount(BigDecimal.valueOf(500))
                .timestamp(Instant.now())
                .status(TransactionStatus.COMPLETED)
                .description("Test deposit")
                .balanceAfter(BigDecimal.valueOf(1500))
                .build();

        List<TransactionResponse> transactions = List.of(transaction);
        when(walletService.getTransactionHistory(eq(walletId), eq(0), eq(20))).thenReturn(transactions);

        mockMvc.perform(get("/api/wallets/{walletId}/transactions", walletId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].walletId", is(walletId.toString())))
                .andExpect(jsonPath("$[0].type", is("DEPOSIT")))
                .andExpect(jsonPath("$[0].amount", is(500)))
                .andExpect(jsonPath("$[0].status", is("COMPLETED")))
                .andExpect(jsonPath("$[0].balanceAfter", is(1500)));
    }
}

