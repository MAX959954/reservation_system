package com.example.reservation_system.registration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class RegistrationRequest {

    private final String username ;
    @Getter
    private final String email ;
    @Getter
    private final String password ;

    public String getFullName(){
        return username ;
    }

}
