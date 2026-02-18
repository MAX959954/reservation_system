package com.example.reservation_system.business_logic.bookings;
import com.example.reservation_system.model.AppUser;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "bookings")
@Setter
@Getter
@NoArgsConstructor
@EqualsAndHashCode

public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY )
    private Long id;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @Getter
    private  BigDecimal total_amount;

    private String currency;

    private  LocalDate  check_in;

    private LocalDate check_out;

    private LocalDate created_at;

    private LocalDate updated_at;

    private Long payment_intent_id;

    private String invoice_no ;

    @ManyToOne
    @JoinColumn (
            name = "user_id" ,
            nullable = false ,
            referencedColumnName = "id"
    )
    private AppUser appUser;

    public  Booking (BookingStatus status , BigDecimal total_amount ,  String currency ,  LocalDate  check_in ,  LocalDate  check_out , LocalDate  created_at , LocalDate  updated_at , Long payment_intent_id , String invoice_no ) {
        this.status = status;
        this.check_in = check_in;
        this.check_out = check_out;
        this.created_at = created_at;
        this.currency = currency ;
        this.total_amount= total_amount;
        this.updated_at = updated_at;
        this.payment_intent_id = payment_intent_id;
        this.invoice_no = invoice_no;
    }

    public Collection<String> getTheBookingStatus(){
        if (status == null) {
            return Collections.emptyList();
        }

        switch (status) {
            case RESERVED :
                return List.of("CANCEL" , "VIEW");
            case CONFIRMED:
                return List.of("VIEW");
            case CHECKED_IN :
                return List.of("CANCEL" , "VIEW");
            case IN_PROGRESS :
                return Collections.emptyList();
            case CHECKED_OUT :
                return Collections.emptyList();
            case CANCELLED :
                return Collections.emptyList();
            case COMPLETED :
                return Collections.emptyList();
            default:
                return List.of("VIEW");
        }
    }

    public void setTotal_amount(BigDecimal newPrice) {
        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) < 0 ) {
            throw new IllegalArgumentException("Total amount cannot be the zero");
        }
        this.total_amount = newPrice;
    }
}
