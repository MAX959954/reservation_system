package com.example.reservation_system.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderUtil {

    public static BCryptPasswordEncoder createEncoder() {
        return new BCryptPasswordEncoder();
    }

}
