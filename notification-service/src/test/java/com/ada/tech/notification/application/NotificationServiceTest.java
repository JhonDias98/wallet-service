package com.ada.tech.notification.application;

import com.ada.tech.notification.domain.TransactionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationSender emailSender;

    @Mock
    private NotificationSender pushSender;

    @InjectMocks
    private NotificationService service;

    @BeforeEach
    void setUp() {
        service = new NotificationService(List.of(emailSender, pushSender));
    }

    @Test
    void shouldSendThroughAllSenders() {
        TransactionEvent event = new TransactionEvent("tx", "user", BigDecimal.TEN);
        service.notify(event);
        verify(emailSender).send(event);
        verify(pushSender).send(event);
    }
}