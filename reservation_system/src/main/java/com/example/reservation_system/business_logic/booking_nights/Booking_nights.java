package com.example.reservation_system.business_logic.booking_nights;

import com.example.reservation_system.business_logic.bookings.Booking;
import com.example.reservation_system.business_logic.room.Room;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "booking_nights")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Booking_nights {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY )
    private Long id;

    @ManyToOne
    @JoinColumn (
            name = "booking_id" ,
            nullable = false ,
            referencedColumnName = "id"
    )
    private Booking booking ;

    @ManyToOne
    @JoinColumn (
            name = "room_id" ,
            nullable = false ,
            referencedColumnName =  "id"
    )

    private Room room;

    private LocalDateTime night_date;
    private int price;

    public Booking_nights(Booking booking, Room room, LocalDateTime night_date, int price) {
        this.booking = booking;
        this.room = room;
        this.night_date = night_date;
        this.price = price;
    }
}
