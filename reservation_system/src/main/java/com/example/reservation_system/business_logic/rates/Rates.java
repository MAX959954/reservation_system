package com.example.reservation_system.business_logic.rates;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table (name ="rates")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class Rates {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String room_type;
    private LocalDateTime start_date;
    private LocalDateTime end_date;
    private int price ;

    public Rates(String room_type, LocalDateTime start_date, LocalDateTime end_date, int price) {
        this.room_type = room_type;
        this.start_date = start_date;
        this.end_date = end_date;
        this.price = price;
    }
}
