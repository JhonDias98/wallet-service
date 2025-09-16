package com.ada.tech.user.controller;

import com.ada.tech.user.application.service.UserService;
import com.ada.tech.user.controller.dto.UserRequest;
import com.ada.tech.user.controller.dto.UserResponse;
import com.ada.tech.user.domain.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Validated @RequestBody UserRequest request) {
        User user = service.create(request);
        return ResponseEntity.ok(new UserResponse(user.getId(), user.getUsername()));
    }
}