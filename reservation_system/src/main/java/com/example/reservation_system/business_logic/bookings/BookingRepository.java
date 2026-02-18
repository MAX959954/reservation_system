package com.example.reservation_system.business_logic.bookings;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking , Long> {
    @SuppressWarnings("null")
    Optional<Booking> findById(Long id);
    
    @Query("SELECT b FROM Booking b WHERE b.id = :id")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Booking> findByIdForUpdate(Long id);
    
    Optional<Booking> findByDateCheckIn (LocalDate check_in);
    Optional<Booking> findByCreatedAt (LocalDate created_at);
    List<Booking> findRoomsBookedBetween(LocalDate check_in ,LocalDate checked_out);

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

    List<Booking> findByUserId(Long userId);
}
