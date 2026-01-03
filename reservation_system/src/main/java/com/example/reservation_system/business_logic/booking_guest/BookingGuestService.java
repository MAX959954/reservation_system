package com.example.reservation_system.business_logic.booking_guest;

import com.example.reservation_system.business_logic.booking_nights.Booking_nights;
import com.example.reservation_system.business_logic.booking_nights.Booking_nights_repository;
import com.example.reservation_system.registration.EmailService;
import org.springframework.stereotype.Service;

@Service
public class BookingGuestService {
    private final BookingGuestRepository bookingGuestRepository;
    private final EmailService emailService;

    public BookingGuestService(BookingGuestRepository bookingGuestRepository, EmailService emailService) {
        this.bookingGuestRepository = bookingGuestRepository;
        this.emailService = emailService;
    }

    public BookinGuests findByFullName (String full_name) {
        return bookingGuestRepository.findByFullName(full_name)
                .orElseThrow(() -> new IllegalStateException("Not found by this name: " + full_name));
    }

    public BookinGuests findByEmail (String email) {
        return bookingGuestRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Not found by this email: " + email ));
    }
}
