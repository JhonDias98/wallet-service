package com.wallet.service.repository;

import com.wallet.service.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    
    Page<Transaction> findByWalletIdOrderByTimestampDesc(UUID walletId, Pageable pageable);
    
    @Query("SELECT t FROM Transaction t WHERE t.walletId = :walletId AND t.timestamp <= :timestamp ORDER BY t.timestamp DESC LIMIT 1")
    Optional<Transaction> findLatestTransactionBeforeTimestamp(@Param("walletId") UUID walletId, @Param("timestamp") Instant timestamp);

}

