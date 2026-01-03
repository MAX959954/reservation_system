package com.example.reservation_system.business_logic.booking_guest;

import com.example.reservation_system.business_logic.bookings.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookingGuestRepository extends JpaRepository<BookinGuests , Long> {
    Optional<BookinGuests> findByFullName (String full_name);
    Optional<BookinGuests> findByEmail (String email);
}
