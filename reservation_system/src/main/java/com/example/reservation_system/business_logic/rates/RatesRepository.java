package com.example.reservation_system.business_logic.rates;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.time.LocalDate;
import java.util.List;

public interface RatesRepository extends JpaRepository<Rates , Long> {
    Optional<Rates> findByRoomType (String room_type);
    Optional<Rates> findByStartDate (LocalDate start_date);

     /**
     * Finds the seasonal rate applicable for a room type on a given date.
     * Returns rates where the date falls within [start_date, end_date].
     */
    @Query("SELECT r FROM Rates r WHERE r.room_type = :roomType AND :date >= r.start_date AND :date <= r.end_date")
    List<Rates> findApplicableRatesForDate(@Param("roomType") String roomType , @Param("date") LocalDate date);

}
