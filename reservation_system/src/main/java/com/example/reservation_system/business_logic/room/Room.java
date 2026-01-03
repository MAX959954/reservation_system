package com.example.reservation_system.business_logic.room;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import java.util.Collection;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class Room {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long Id;

    private String number;

    private String name;

    private String type;

    private int capacity;

    private int base_price ;

    @Getter
    private BigDecimal pricePerNight;

    @Enumerated(EnumType.STRING)
    private RoomStatus status;

    public Room(String number, String name, String type, int capacity, int base_price, RoomStatus status) {
        this.number = number;
        this.name = name;
        this.type = type;
        this.capacity = capacity;
        this.base_price = base_price;
        this.status = status;
    }

    public Collection<String> getAllowedActions () {
        if (status == null) {
            return Collections.emptyList();
        }

        switch (status) {
            case AVAILABLE :
                return List.of("BOOK" , "VIEW");
            case OCCUPIED :
                return List.of("VIEW");
            case RESERVED :
                return List.of("CHECKOUT" , "VIEW");
            case CLEANING:
                return List.of("WAIT" , "VIEW");
            case OUT_OF_SERVICE:
                return List.of("MAINTENANCE" , "VIEW" );
            default:
                return List.of("VIEW");
        }
    }
}
