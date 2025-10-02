package com.next.move.security;

import com.next.move.models.UserProfile;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
public class RefreshToken {
    @Id
    @GeneratedValue
    private Long id;
    @Column(unique=true) private String token;
    @ManyToOne
    private UserProfile user;
    private Instant expiryDate;
    private String deviceId; // optional
    private boolean revoked;
}
