package com.example.reservation_system.business_logic.bookings;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingCmd {
    
    private LocalDate checkIn;
    private LocalDate checkOut;
    private List<Long> roomIds; // List of room IDs for multi-room booking
    private List<Integer> adults; // Adults count per room
    private List<Integer> children; // Children count per room
    private BigDecimal totalAmount;
    private String currency = "USD";
    private Long userId;
    
    public CreateBookingCmd(LocalDate checkIn, LocalDate checkOut, List<Long> roomIds, 
                          List<Integer> adults, List<Integer> children, 
                          BigDecimal totalAmount, Long userId) {
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.roomIds = roomIds;
        this.adults = adults;
        this.children = children;
        this.totalAmount = totalAmount;
        this.userId = userId;
    }
}
