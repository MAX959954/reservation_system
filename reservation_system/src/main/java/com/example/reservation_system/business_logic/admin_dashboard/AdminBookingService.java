package com.example.reservation_system.business_logic.admin_dashboard;

import com.example.reservation_system.business_logic.bookings.Booking;
import com.example.reservation_system.business_logic.bookings.BookingRepository;
import com.example.reservation_system.business_logic.payments.PaymentRepository;
import com.example.reservation_system.business_logic.pricing.PricingService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class AdminBookingService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final PricingService pricingService;

    public AdminBookingService(BookingRepository bookingRepository,
                               PaymentRepository paymentRepository,
                               PricingService pricingService) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.pricingService = pricingService;
    }

    @Transactional
    public Booking modifyBooking(
            Long bookingId,
            LocalDate newCheckIn,
            LocalDate newCheckOut
    ) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (newCheckOut.isBefore(newCheckIn)) {
            throw new IllegalArgumentException("Check-out must be after check-in");
        }

        // ✔ Booking expects LocalDate, not LocalDateTime
        booking.setCheck_in(LocalDate.from(newCheckIn));
        booking.setCheck_out(LocalDate.from(newCheckOut));

        // Recalculate total using seasonal rates and booking's rooms (dates already set above)
        BigDecimal newPrice = pricingService.calculateTotalForBooking(booking);
        booking.setTotal_amount(newPrice);

        return bookingRepository.save(booking);
    }

    @Transactional
    public BigDecimal cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found"));

        long hoursBeforeCheckIn = ChronoUnit.HOURS.between(
                LocalDateTime.now(),
                booking.getCheck_in().atStartOfDay()
        );

        BigDecimal refund;

        if (hoursBeforeCheckIn > 48) {
            refund = booking.getTotal_amount();
        } else if (hoursBeforeCheckIn > 0) {
            refund = booking.getTotal_amount().multiply(BigDecimal.valueOf(0.5));
        } else {
            refund = BigDecimal.ZERO;
        }

        // (optional) paymentRepository.saveRefund(...)
        return refund;
    }


}
