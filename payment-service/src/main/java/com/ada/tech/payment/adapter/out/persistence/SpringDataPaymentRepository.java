package com.ada.tech.payment.adapter.out.persistence;

import com.ada.tech.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataPaymentRepository extends JpaRepository<Payment, Long> {
}