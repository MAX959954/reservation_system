package com.example.reservation_system.controller;

import com.example.reservation_system.model.AppUser;
import com.example.reservation_system.model.AppUserRepository;
import com.example.reservation_system.model.AppUserService;
import com.example.reservation_system.registration.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/req/signup")
public class RegistrationController {

    private final AppUserService userService;
    private final EmailService emailService;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public RegistrationController(AppUserService userService, EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }

    @PostMapping(value = "/req/signup", consumes = "application/json")
    public AppUser createUser(@RequestBody AppUser appUser) {
        appUser.setPassword(passwordEncoder.encode(appUser.getPassword()));
        return appUserRepository.save(appUser);
    }

}
