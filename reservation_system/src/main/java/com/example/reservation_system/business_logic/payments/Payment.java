package com.example.reservation_system.business_logic.payments;

import com.example.reservation_system.business_logic.bookings.Booking;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "payments")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY )
    private Long id;

    private String provider;

    private String provider_ref;

    private int amount;

    private LocalDate created_at ;

    @Enumerated(EnumType.STRING)
    private PayementStatus status;

    @ManyToOne
    @JoinColumn(
            name = "booking_id" ,
            nullable = false ,
            referencedColumnName = "id"
    )

    private Booking booking;

    public Payment(String provider , String provider_ref , int amount ,PayementStatus status , LocalDate created_at  ) {
        this.provider = provider;
        this.amount = amount;
        this.created_at = created_at;
        this.status = status;
        this.provider_ref = provider_ref;
    }

    public Collection<String> getThePaymentStatus() {
        if (status == null) {
            return Collections.emptyList();
        }

        switch (status) {
            case PROCESSING :
                return List.of("VIEW");
            case  COMPLETED :
                return List.of("VIEW");
            case REJECTED:
                return Collections.emptyList();
            default:
                throw new IllegalStateException("Unexpected payment status: " + status);
        }
    }
}
