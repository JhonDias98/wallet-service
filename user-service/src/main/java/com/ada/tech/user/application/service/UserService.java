package com.ada.tech.user.application.service;

import com.ada.tech.user.application.service.dto.UserCreatedEvent;
import com.ada.tech.user.controller.dto.UserRequest;
import com.ada.tech.user.domain.entity.User;
import com.ada.tech.user.domain.repository.UserRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository repository;
    private final RabbitTemplate rabbitTemplate;

    public UserService(UserRepository repository, RabbitTemplate rabbitTemplate) {
        this.repository = repository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public User create(UserRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(request.password());
        User saved = repository.save(user);
        rabbitTemplate.convertAndSend("user.created", new UserCreatedEvent(saved.getId(), saved.getUsername()));
        return saved;
    }
}