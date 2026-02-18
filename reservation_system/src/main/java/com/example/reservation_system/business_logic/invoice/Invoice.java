package com.example.reservation_system.business_logic.invoice;

import com.example.reservation_system.business_logic.bookings.Booking;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table (name = "invoices")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class Invoice {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long Id;

    @ManyToOne
    @JoinColumn(
            name = "booking_id",
            nullable = false ,
            referencedColumnName =  "id"
    )

    private Booking booking ;

    private String pdf_path ;
    private LocalDate issued_at;

    public Invoice(Booking booking, String pdf_path, LocalDate issued_at) {
        this.booking = booking;
        this.pdf_path = pdf_path;
        this.issued_at = issued_at;
    }
}
