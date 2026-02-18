package com.example.reservation_system.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;

import com.example.reservation_system.business_logic.bookings.Booking;
import com.example.reservation_system.business_logic.bookings.BookingStatus;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter 
public class BookingResponse {
    @Getter
    private Long id;
    @Getter 
    private BookingStatus status; 
    @Getter 
    private BigDecimal totalAmount ; 
    @Getter 
    private String currency; 
    @Getter 
    private LocalDate checkIn;
    @Getter 
    private LocalDate checkOut; 
    @Getter 
    private LocalDate updatedAt;
    @Getter 
    private Collection<String> actions;

    public BookingResponse (Booking booking) {
        this.id = booking.getId();
        this.status = booking.getStatus();
        this.totalAmount = booking.getTotal_amount();
        this.currency = booking.getCurrency();
        this.checkIn = booking.getCheck_in(); 
        this.checkOut = booking.getCheck_out();
        this.updatedAt = booking.getUpdated_at();
        this.actions = booking.getTheBookingStatus();
    }

}
