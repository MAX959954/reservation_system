package com.example.reservation_system.token;  // Adjust if different

import com.example.reservation_system.model.AppUser;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity  // Required: Marks this as a JPA entity
@Table(name = "confirmation_tokens")  // Optional: Custom table name; defaults to "confirmation_token"
@Data  // Optional: Lombok for getters/setters (add dependency if needed)
@NoArgsConstructor  // Optional: Lombok for no-arg constructor
@AllArgsConstructor  // Optional: Lombok for all-arg constructor
public class ConfirmationToken {

    @Id  // Required: Primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-increment (use AUTO or UUID if preferred)
    private Long id;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime confirmedAt;  // Nullable by default

    @ManyToOne  // Or @OneToOne if strictly one-to-one
    @JoinColumn(
            name = "user_id",  // Changed to conventional name (adjust back to "users_id" if intentional; IDE may prefer this)
            nullable = false,
            referencedColumnName = "id"  // Explicitly reference AppUser's PK column (helps IDE validation)
    )
    private AppUser appUser;

    // Constructor (remove if using Lombok @AllArgsConstructor)
    public ConfirmationToken(String token, LocalDateTime createdAt, LocalDateTime expiresAt, AppUser appUser) {
        this.token = token;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.appUser = appUser;
    }
}