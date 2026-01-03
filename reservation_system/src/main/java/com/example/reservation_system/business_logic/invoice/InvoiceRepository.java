package com.example.reservation_system.business_logic.invoice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice , Long> {
    Optional<Invoice> findByPDFPath(String pdf_path);
    Optional<Invoice> findByIssued_at(LocalDateTime issued_at);
}
