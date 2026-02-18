package com.example.reservation_system.business_logic.booking_rooms;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.reservation_system.business_logic.bookings.Booking;
import java.util.List;

@Repository 
public interface BookingRoomsRepository extends JpaRepository<BookingRooms , Long> {

    List<BookingRooms> findByBooking(Booking booking);

    List<BookingRooms> findByBookingId(Long bookingId);

    List<BookingRooms> findByBookingIn(List<Booking> bookings);

    void deleteByBookingId(Long bookingId);
    
}
