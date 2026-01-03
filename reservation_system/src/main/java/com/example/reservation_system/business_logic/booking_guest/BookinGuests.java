package com.example.reservation_system.business_logic.booking_guest;

import com.example.reservation_system.business_logic.bookings.Booking;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table (name = "booking_guests")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class BookinGuests {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY )
    private Long id;

    private String full_name ;
    private String email;
    private String phone;

    @ManyToOne
    @JoinColumn (
            name = "booking_id" ,
            nullable = false ,
            referencedColumnName = "id"
    )
    private Booking booking ;


    public BookinGuests(String full_name, String email, String phone) {
        this.full_name = full_name;
        this.email = email;
        this.phone = phone;
    }
}
