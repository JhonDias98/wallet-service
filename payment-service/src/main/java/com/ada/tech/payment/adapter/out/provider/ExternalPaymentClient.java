package com.ada.tech.payment.adapter.out.provider;

import com.ada.tech.payment.application.port.out.ExternalPaymentPort;
import com.ada.tech.payment.domain.Payment;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

@Component
public class ExternalPaymentClient implements ExternalPaymentPort {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public ExternalPaymentClient(RestTemplateBuilder builder,
                                 @Value("${external.provider.base-url:http://localhost:9561}") String baseUrl) {
        this.restTemplate = builder.build();
        this.baseUrl = baseUrl;
    }

    @Override
    public boolean process(Payment payment) {
        try {
            return doProcess(payment).get();
        } catch (Exception e) {
            return false;
        }
    }

    @CircuitBreaker(name = "externalProvider", fallbackMethod = "fallback")
    @Retry(name = "externalProvider")
    @TimeLimiter(name = "externalProvider")
    public CompletableFuture<Boolean> doProcess(Payment payment) {
        ExternalPaymentRequest request = new ExternalPaymentRequest(payment.getWalletId(), payment.getAmount());
        return CompletableFuture.supplyAsync(() -> {
            restTemplate.postForEntity(baseUrl + "/payments", request, Void.class);
            return true;
        });
    }

    private CompletableFuture<Boolean> fallback(Payment payment, Throwable throwable) {
        return CompletableFuture.completedFuture(false);
    }

    private record ExternalPaymentRequest(Long walletId, BigDecimal amount) {}
}