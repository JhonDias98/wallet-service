package com.ada.tech.notification.application;

import com.ada.tech.notification.domain.TransactionEvent;
import com.ada.tech.notification.infrastructure.EmailNotificationSender;
import com.ada.tech.notification.infrastructure.PushNotificationSender;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class TransactionEventListenerIntegrationTest {

    @Autowired
    private TransactionEventListener listener;

    @MockitoSpyBean
    private EmailNotificationSender emailSender;

    @MockitoSpyBean
    private PushNotificationSender pushSender;

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    void shouldProcessEventAndIncrementMetric() {
        TransactionEvent event = new TransactionEvent("id", "user", BigDecimal.ONE);
        double before = meterRegistry.counter("notification.events.processed").count();
        listener.onMessage(event);
        verify(emailSender, times(1)).send(event);
        verify(pushSender, times(1)).send(event);
        assertEquals(before + 1, meterRegistry.counter("notification.events.processed").count());
    }
}