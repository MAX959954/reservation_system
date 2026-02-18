package com.example.reservation_system.business_logic.booking_rooms;

import com.example.reservation_system.business_logic.bookings.Booking;
import com.example.reservation_system.business_logic.room.Room;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "booking_rooms")
@Getter 
@Setter 
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BookingRooms {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id ; 

    @ManyToOne 
    @JoinColumn(
        name = "booking_id" , 
        nullable = false , 
        referencedColumnName = "id"
    )

    private Booking booking ; 

    @ManyToOne 
    @JoinColumn (
        name = "room_id" , 
        nullable = false ,
        referencedColumnName = "id"
    )

    private Room room ; 
    private int adults = 1 ; 
    private int children = 0 ;

    private LocalDate created_at = LocalDate.now();

    public BookingRooms(Booking booking , Room room , int adults , int children , LocalDate  created_at) {
        this.booking = booking;
        this.room = room;
        this.adults = adults;
        this.children = children;
        this.created_at = created_at;
    }

}
