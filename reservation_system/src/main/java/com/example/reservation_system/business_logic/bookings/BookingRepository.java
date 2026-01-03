package com.example.reservation_system.business_logic.bookings;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking , Long> {
    Optional<Booking> findById(Long id);
    Optional<Booking> findByDateCheckIn (LocalDateTime check_in);
    Optional<Booking> findByCreatedAt (LocalDateTime created_at);
    List<Booking> findRoomsBookedBetween(LocalDateTime check_in , LocalDateTime checked_out);

    @Query ("SELECT count(b) , 0  FROM Booking b WHERE b.status = 'CONFIRMED'")
    long getTotalBookings();

    @Query ("""
        SELECT SUM(DATEDIFF(b.check_in , b.check_out))
        FROM Booking b 
        WHERE b.status = 'CONFIRMED'
        """)
    Long getTotalBookedNights();

    @Query ("SELECT b FROM Booking b ORDER BY b.created_at DESC")
    List<Booking> findBookings();

    @Query ("SELECT COUNT(b) FROM Booking b WHERE b.status = 'CONFIRMED'")
    long getConfirmedBookings();

    @Query ("SELECT count(b) FROM Booking b WHERE b.status = 'CANCALED'")
    long getCancelledBookings();
}
