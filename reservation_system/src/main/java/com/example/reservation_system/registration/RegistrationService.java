package com.example.reservation_system.registration;

import com.example.reservation_system.model.AppUser;
import com.example.reservation_system.model.AppUserRole;
import com.example.reservation_system.model.AppUserService;
import com.example.reservation_system.token.ConfirmationToken;
import com.example.reservation_system.token.ConfirmationTokenService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class RegistrationService {

    private final AppUserService appUserService;
    private final EmailValidator emailValidator;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailService emailService;

    public String register(RegistrationRequest registrationRequest) {
        boolean isValidEmail = emailValidator.test(registrationRequest.getEmail());

        if (!isValidEmail) {
            throw new IllegalStateException("Invalid email");
        }

        String token = appUserService.signUpUser(
                new AppUser(
                        registrationRequest.getUsername(),
                        registrationRequest.getEmail(),
                        registrationRequest.getPassword(),
                        AppUserRole.GUEST
                )
        );

        String link = "http://localhost:8080/registration?token=" + token;
        emailService.send(registrationRequest.getEmail(),
                "Confirm your email",  // Added missing subject parameter
                buildEmail(registrationRequest.getUsername(), link));

        return token;
    }

    @Transactional
    public String confirmToken(String token) {
        ConfirmationToken confirmationToken = confirmationTokenService
                .getToken(token)
                .orElseThrow(() -> new IllegalStateException("Invalid token"));

        if (confirmationToken.getConfirmedAt() != null) {
            throw new IllegalStateException("Token already confirmed");
        }

        LocalDateTime expiredAt = confirmationToken.getExpiresAt();

        if (expiredAt.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Token expired");
        }

        confirmationTokenService.setConfirmationToken(token);  // Assuming this sets confirmedAt; if not, replace with confirmationTokenService.setConfirmedAt(token, LocalDateTime.now());
        appUserService.enableAppUser(confirmationToken.getAppUser().getEmail());

        return "confirmed";
    }

    private String buildEmail(String name, String link) {
        // Basic HTML email template for confirmation
        return "<div style=\"font-family: Helvetica,Arial,sans-serif;min-width:1000px;overflow:auto;line-height:2\">"
                + "<div style=\"margin:50px auto;width:70%;padding:20px 0\">"
                + "<div style=\"border-bottom:1px solid #eee\">"
                + "<a href=\"\" style=\"font-size:1.4em;color: #00466a;text-decoration:none;font-weight:600\">Your Brand</a>"
                + "</div>"
                + "<p style=\"font-size:1.1em\">Hi, " + name + ",</p>"
                + "<p>Thank you for registering. Please click on the below link to activate your account:</p>"
                + "<h2 style=\"background: #00466a;margin: 0 auto;width: max-content;padding: 0 10px;color: #fff;border-radius: 4px;\">"
                + "<a href=\"" + link + "\" style=\"color: #fff; text-decoration:none;\">Activate Now</a></h2>"
                + "<p style=\"font-size:0.9em;\">Regards,<br />Your Brand</p>"
                + "<hr style=\"border:none;border-top:1px solid #eee\" />"
                + "<div style=\"float:right;padding:8px 0;color:#aaa;font-size:0.8em;line-height:1;font-weight:300\">"
                + "<p>Your Brand Inc</p>"
                + "<p>1600 Amphitheatre Parkway</p>"
                + "<p>California</p>"
                + "</div>"
                + "</div>"
                + "</div>";
    }
}