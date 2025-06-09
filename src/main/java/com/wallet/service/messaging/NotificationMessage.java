package com.wallet.service.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {
    private Long userId;
    private UUID walletId;
    private String type;
    private BigDecimal amount;
    private UUID referenceId;
    private String description;
}
