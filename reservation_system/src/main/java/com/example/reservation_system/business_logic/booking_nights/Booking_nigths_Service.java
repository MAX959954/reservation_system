package com.example.reservation_system.business_logic.booking_nights;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class Booking_nigths_Service {
    private final Booking_nights_repository bookingNightsRepository;

    public Booking_nigths_Service(Booking_nights_repository bookingNightsRepository) {
        this.bookingNightsRepository = bookingNightsRepository;
    }

    public Booking_nights findByPrice (int price) {
        return bookingNightsRepository.findByPrice(price)
                .orElseThrow( ()-> new IllegalStateException("not found by this price " + price));
    }

    public Booking_nights findByNightDate (LocalDateTime night_date ) {
        return bookingNightsRepository.findByNightDate(night_date)
                .orElseThrow(() -> new IllegalStateException("not found by this night date " + night_date ));
    }
}
