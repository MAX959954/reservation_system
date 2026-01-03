package com.example.reservation_system.business_logic.payments;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment , Long> {

    Optional<Payment> findByProvider (String provider);
    Optional<Payment> findByProviderRef (String provider_ref);

    @Query("SELECT COALESCE(count(p.amount) , 0)FROM Payment p")
    BigDecimal getTotalAmount();

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED'")
    BigDecimal getTotalRevenue();
}
