package com.notificarion.service.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class NotificationMessage {
    private Long userId;
    private UUID walletId;
    private String type;
    private BigDecimal amount;
    private UUID referenceId;
    private String description;
}
