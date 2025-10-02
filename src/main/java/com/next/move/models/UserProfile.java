package com.next.move.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String givenName;
    private String phone;
    private String password;

    private Boolean phoneVerified;
    private String phoneVerificationCode;
    private Integer subscriptionPlan;
    private String subscriptionId;
    private Integer subscriptionStatus;

    //@Column(nullable = false)
    private Instant lastStatusUpdate = Instant.now();

    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.EAGER)
    private List<Goals> goalsList;

    @Transient // not persisted
    private String originalPassword;

    @PostLoad
    private void storeOriginalPassword() {
        this.originalPassword = this.password; // keep copy from DB
    }

    @PreUpdate
    private void validatePassword() {
        if (password == null || password.length() < 8) {
            this.password = this.originalPassword; // revert back
        }
    }

 }
