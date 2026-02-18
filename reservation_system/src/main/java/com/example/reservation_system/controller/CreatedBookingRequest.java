package com.example.reservation_system.controller;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreatedBookingRequest {
    @NotBlank
    @Setter
    @Getter
    private String checkIn;

    @NotBlank
    @Setter
    @Getter
    private String checkOut;

    @NotEmpty
    @Setter
    @Getter
    private List<Long> roomIds;

    @NotEmpty
    @Setter
    @Getter
    private List<Integer> adults;

    @NotNull 
    @Setter
    @Getter
    private List<Integer> children;

    @NotNull 
    @DecimalMin("0.0")
    @Setter
    @Getter
    private BigDecimal totalAmount; 

    @Setter
    @Getter
    private String currency = "USD";
}
