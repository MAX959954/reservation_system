package com.example.reservation_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ContenotController {

    @GetMapping("/login")
    public String login (){
        return "login";
    }

    @GetMapping("/req/signup")
    public String singup (){
        return "signup";
    }

    @GetMapping("/index")
    public String home(){
        return "index";
    }

}
