package com.ada.tech.payment.adapter.in.controller;

import com.ada.tech.payment.adapter.in.controller.dto.PaymentRequest;
import com.ada.tech.payment.adapter.in.controller.dto.PaymentResponse;
import com.ada.tech.payment.application.port.in.PaymentUseCase;
import com.ada.tech.payment.domain.Payment;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentUseCase useCase;

    public PaymentController(PaymentUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/deposit")
    public ResponseEntity<PaymentResponse> deposit(@Valid @RequestBody PaymentRequest request) {
        Payment payment = new Payment();
        payment.setWalletId(request.walletId());
        payment.setAmount(request.amount());
        Payment result = useCase.deposit(payment);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(result));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<PaymentResponse> withdraw(@Valid @RequestBody PaymentRequest request) {
        Payment payment = new Payment();
        payment.setWalletId(request.walletId());
        payment.setAmount(request.amount());
        Payment result = useCase.withdraw(payment);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(result));
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(payment.getId(), payment.getWalletId(), payment.getAmount(), payment.getType(), payment.getStatus());
    }
}