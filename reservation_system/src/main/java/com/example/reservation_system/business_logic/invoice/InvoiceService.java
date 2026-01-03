package com.example.reservation_system.business_logic.invoice;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;

    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public Invoice findByPDFPath(String pdf_path) {
        return invoiceRepository.findByPDFPath(pdf_path)
                .orElseThrow(() -> new IllegalStateException("Not find by this pdf path" + pdf_path ));
    }

    public Invoice findByIssued_at (LocalDateTime issued_at) {
        return invoiceRepository.findByIssued_at(issued_at)
                .orElseThrow(() -> new IllegalStateException("Not found by this issued " + issued_at));
    }
}
