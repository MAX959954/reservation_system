package com.example.reservation_system.business_logic.booking_nights;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface Booking_nights_repository extends JpaRepository<Booking_nights , Long> {
    Optional<Booking_nights> findByPrice(int price);
    Optional<Booking_nights> findByNightDate(LocalDateTime night_date);
}
