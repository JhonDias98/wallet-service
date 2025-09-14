package com.ada.tech.wallet.repository;

import com.ada.tech.wallet.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    
    boolean existsByUserId(Long userId);
}

