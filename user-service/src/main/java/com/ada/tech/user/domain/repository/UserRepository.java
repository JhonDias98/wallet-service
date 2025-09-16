package com.ada.tech.user.domain.repository;

import java.util.Optional;

import com.ada.tech.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}