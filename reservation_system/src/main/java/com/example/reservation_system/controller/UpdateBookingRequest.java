package com.example.reservation_system.controller;
import jakarta.validation.constraints.NotBlank;

public class UpdateBookingRequest {
    @NotBlank 
    private String checkIn;

    @NotBlank 
    private String checkOut;

    public String getCheckIn() {
        return  checkIn;
    }

    public String getCheckOut(){
        return  checkOut;
    }

    public void setCheckIn(String checkIn){
        this.checkIn = checkIn;
    }

    public void setCheckOut(String checkOut){
        this.checkOut = checkOut;
    }
}
