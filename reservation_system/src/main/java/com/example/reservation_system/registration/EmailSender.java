package com.example.reservation_system.registration;


public interface EmailSender {

    void send(String to, String subject, String body);
}
