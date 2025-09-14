package com.ada.tech.payment.adapter.out.provider;

import com.ada.tech.payment.domain.Payment;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalPaymentClientContractTest {

    private WireMockServer wireMockServer;
    private ExternalPaymentClient client;

    @BeforeEach
    void setup() {
        wireMockServer = new WireMockServer();
        wireMockServer.start();
        configureFor("localhost", wireMockServer.port());
        client = new ExternalPaymentClient(new RestTemplateBuilder(), "http://localhost:" + wireMockServer.port());
    }

    @AfterEach
    void teardown() {
        wireMockServer.stop();
    }

    @Test
    void processPaymentReturnsTrueOn200() {
        stubFor(post(urlEqualTo("/payments")).willReturn(aResponse().withStatus(200)));
        Payment payment = new Payment();
        payment.setWalletId(1L);
        payment.setAmount(new BigDecimal("3.00"));
        assertTrue(client.process(payment));
    }
}