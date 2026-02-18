package com.example.reservation_system.business_logic.payments;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment findByProvider (String provider) {
        return paymentRepository.findByProvider(provider)
                .orElseThrow(() -> new IllegalStateException("Not found payment by this provider " + provider));
    }

    public Payment findByProviderRef (String provider_ref) {
        return paymentRepository.findByProviderRef(provider_ref)
                .orElseThrow(() -> new IllegalStateException("Not found payment by this provider ref " + provider_ref));
    }

    public Payment CreatePayment (Payment payment) {
        if (payment.getProvider() == null || payment.getProvider().isBlank()){
            throw new IllegalArgumentException("Provider must not be empty");
        }
        payment.setCreated_at(LocalDate.now());
        return paymentRepository.save(payment);
    }

    public BigDecimal getTotalRevenue() {
        BigDecimal total_revenue = paymentRepository.getTotalRevenue();
        return total_revenue != null ? total_revenue : BigDecimal.ZERO;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal total_count = paymentRepository.getTotalAmount();
        return total_count != null ? total_count : BigDecimal.ZERO;
    }
}
