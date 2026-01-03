package com.example.reservation_system.registration;

import org.springframework.stereotype.Service;
import java.util.function.Predicate;  // Correct import

@Service
public class EmailValidator implements Predicate<String> {

    @Override
    public boolean test(String s) {
        return true;  // Placeholder
    }
}