package com.example.reservation_system.controller;

import com.example.reservation_system.model.AppUserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
 
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController 
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager; 
    private final AppUserService appUserService;
    private final String SECKRET_KEY = "your-secret-key";
    private final long EXPIRATION_TIME = 86400000; //24 hours 

    @Autowired 
    public AuthController(AuthenticationManager authenticationManager ,AppUserService appUserService ){
        this.authenticationManager = authenticationManager;
        this.appUserService = appUserService;
    }


    @PostMapping("/login")
    public ResponseEntity<?> login (@RequestBody LoginRequest loginRequest){
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = generateToken(userDetails.getUsername());

            Map<String , Object> response = new HashMap<>(); 
            response.put("token" , token);
            response.put("username" , userDetails.getUsername());
            response.put("expiresIn" , EXPIRATION_TIME);

            return ResponseEntity.ok(response);

        }catch (Exception e) {
           Map<String , String> error = new HashMap<>();
            error.put("Error" , "Invalid credantials");
            return ResponseEntity.status(401).body(error);
        }
    }

    @SuppressWarnings("deprecation")
    private String generateToken(String username) {
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .signWith(SignatureAlgorithm.HS256 , SECKRET_KEY )
            .compact();
    }

    public static class LoginRequest {
        private String username ; 
        private String password;

        public String getUsername() { return username;}
        public String getPassword() { return password;}

        public void setUsername(String username) {this.username = username;}
        public void setPassword(String password) {this.password = password;}
    }

}
