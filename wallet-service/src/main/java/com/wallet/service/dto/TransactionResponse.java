package com.wallet.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wallet.service.model.TransactionStatus;
import com.wallet.service.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionResponse {
    
    private UUID id;
    private UUID walletId;
    private TransactionType type;
    private BigDecimal amount;
    private Instant timestamp;
    private TransactionStatus status;
    private UUID referenceId;
    private String description;
    private BigDecimal balanceAfter;
}

