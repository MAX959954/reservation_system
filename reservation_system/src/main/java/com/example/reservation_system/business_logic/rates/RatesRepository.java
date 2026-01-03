package com.example.reservation_system.business_logic.rates;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RatesRepository extends JpaRepository<Rates , Long> {
    Optional<Rates> findByRoomType (String room_type);
    Optional<Rates> findByStartDate (String start_date);
}
