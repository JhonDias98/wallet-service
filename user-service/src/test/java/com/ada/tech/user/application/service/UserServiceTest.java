package com.ada.tech.user.application.service;

import com.ada.tech.user.application.service.dto.UserCreatedEvent;
import com.ada.tech.user.controller.dto.UserRequest;
import com.ada.tech.user.domain.entity.User;
import com.ada.tech.user.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;


    @InjectMocks
    private UserService userService;

    @Test
    void shouldEncodePasswordAndPublishEvent() {

        User saved = new User(1L, "alice", "secret");
        UserCreatedEvent event = new UserCreatedEvent(saved.getId(), saved.getUsername());
        when(userRepository.save(Mockito.any(User.class))).thenReturn(saved);

        userService.create(new UserRequest("user", "secret"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isNotEqualTo("secret");
        verify(rabbitTemplate).convertAndSend("user.created", event);
    }
}