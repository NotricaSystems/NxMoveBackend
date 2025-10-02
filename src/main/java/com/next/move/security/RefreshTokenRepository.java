package com.next.move.security;

import com.next.move.models.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    int deleteByExpiryDateBefore(Instant now);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :id AND rt.revoked = false")
    Optional<RefreshToken> findByUserId(Long id);

    // Optional if you want single active token per user
    void deleteByUserId(Long id);
}

