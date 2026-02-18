package com.example.reservation_system.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode

//UserDetails - interface from Spring Security for basic user authentication 
// and authorization
public class AppUser implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // simpler and consistent
    private Long id;

    @Setter
    private String username;
    @Setter
    private String full_name;
    @Setter
    private String email;
    @Setter
    private String password;
    @Setter
    private LocalDate created_at;

    @Setter
    private boolean locked = false;
    @Setter
    private boolean enabled = true;

    @Enumerated(EnumType.STRING)
    private AppUserRole userRole;

    public AppUser(String username,  String full_name , String email, String password , LocalDate created_at , AppUserRole userRole) {
        this.username = username;
        this.full_name = full_name;
        this.email = email;
        this.password = password;
        this.created_at = created_at;
        this.userRole = userRole;
    }

    //define the role for user (ROLE_ prefix required for hasRole() in @PreAuthorize)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + userRole.name())
        );
    }

    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return !locked; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return enabled; }
}

