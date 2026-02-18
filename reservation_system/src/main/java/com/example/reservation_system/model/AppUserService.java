package com.example.reservation_system.model;

import com.example.reservation_system.token.ConfirmationToken;
import com.example.reservation_system.token.ConfirmationTokenService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AppUserService implements UserDetailsService {

    private final AppUserRepository appUserRepository;
    private final ConfirmationTokenService confirmationTokenService;
    private final PasswordEncoder passwordEncoder;

    public AppUserService (AppUserRepository appUserRepository ,  ConfirmationTokenService confirmationTokenService , PasswordEncoder passwordEncoder){
        this.appUserRepository = appUserRepository;
        this.confirmationTokenService = confirmationTokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<AppUser> user = appUserRepository.findByUsername(username);

        if (user.isPresent()){
            var userObject = user.get();
            return User.builder()
                    .username(userObject.getUsername())
                    .password(userObject.getPassword())
                    .build();
        }else {
            throw  new UsernameNotFoundException("Unimplemented username:  " + username);
        }
    }

    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException{
        Optional<AppUser> Email = appUserRepository.findByEmail(email);

        if (Email.isPresent()) {
            var userObject = Email.get();
            return User.builder()
                    .username(userObject.getUsername())
                    .password(userObject.getPassword())
                    .build();
        }else {
            throw new UsernameNotFoundException("Not found this email" + email);
        }
    }

    public AppUser findById(Long userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found with id: " + userId));
    }

    public Optional<AppUser> findByUsername(String username) {
        return appUserRepository.findByUsername(username);
    }

    public String signUpUser(AppUser appUser) {
        boolean userExists = appUserRepository.findByUsername(appUser.getUsername()).isPresent();

        if (userExists) {
            throw new IllegalStateException("User already in use: " + appUser.getUsername() + ", " + appUser.getEmail());
        }

        String encodedPassword = passwordEncoder.encode(appUser.getPassword());
        appUser.setPassword(encodedPassword);
        appUser.setLocked(true);  // Assuming new users start locked until confirmed
        appUserRepository.save(appUser);

        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(15),
                appUser
        );

        confirmationTokenService.saveConfirmationToken(confirmationToken);
        return token;
    }

    public void  enableAppUser(String email){
        AppUser appUser = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("user not found"));
        appUser.setLocked(false);
        appUserRepository.save(appUser);
    }

    public long getAllUsers() {
        long allUsers = appUserRepository.getAllUsers();
        return allUsers;
    }

}
