package com.wallet.service.service;

import com.wallet.service.dto.TransferRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Simulates sending transfer notifications using virtual threads
 * and text block templates.
 */
@Service
@Slf4j
public class NotificationService {

    public void sendTransferNotification(TransferRequest request) {
        Thread.startVirtualThread(() -> {
            String email = """
                    Olá,

                    Uma transferência foi realizada entre as carteiras.
                    Valor: %s
                    Carteira de origem: %s
                    Carteira de destino: %s

                    Obrigado,
                    Equipe Wallet
                    """.formatted(
                    request.amount(),
                    request.sourceWalletId(),
                    request.destinationWalletId()
            );
            log.info("Sending email notification:\n{}", email);
        });
    }
}

