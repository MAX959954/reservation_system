package com.example.reservation_system.business_logic.rates;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class RatesService {
    private final RatesRepository ratesRepository;

    public RatesService(RatesRepository ratesRepository) {
        this.ratesRepository = ratesRepository;
    }

    public Rates findByRoomType (String room_type) {
        return ratesRepository.findByRoomType(room_type)
                .orElseThrow(() -> new IllegalStateException("Not found by this room type " + room_type));
    }

    public Rates findByStartDate(LocalDate start_date) {
        return ratesRepository.findByStartDate(start_date)
                .orElseThrow(() -> new IllegalStateException("Not found by this start date " + start_date));
    }
}
