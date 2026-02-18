package com.example.reservation_system.business_logic.rates;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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
    private LocalDate start_date;
    private LocalDate end_date;
    private int price ;

    public Rates(String room_type,  LocalDate start_date,  LocalDate end_date, int price) {
        this.room_type = room_type;
        this.start_date = start_date;
        this.end_date = end_date;
        this.price = price;
    }
}
