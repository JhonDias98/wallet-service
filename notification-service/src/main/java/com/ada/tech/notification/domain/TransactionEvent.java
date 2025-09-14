package com.ada.tech.notification.domain;

import java.math.BigDecimal;

public record TransactionEvent(String transactionId, String userId, BigDecimal amount) {
}