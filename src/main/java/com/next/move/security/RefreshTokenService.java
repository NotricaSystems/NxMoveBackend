package com.next.move.security;

import com.next.move.models.UserProfile;
import com.next.move.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.Getter;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Getter
public class RefreshTokenService {

    @Value("${app.refreshExpirationMs}")
    private long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create a new refresh token for a user.
     */
    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // check existing token
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUserId(user.getId());

        if (existingToken.isPresent() && existingToken.get().getExpiryDate().isAfter(Instant.now())) {
            return existingToken.get(); // still valid
        }

        // optionally delete old tokens if you want single active token per user
        refreshTokenRepository.deleteByUserId(user.getId());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString() + "-" + UUID.randomUUID());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Verify a refresh token string: valid, not expired, not revoked.
     */
    @Transactional
    public RefreshToken verify(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            throw new IllegalStateException("Refresh token has been revoked");
        }

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            throw new IllegalStateException("Refresh token expired");
        }

        return refreshToken;
    }

    /**
     * Revoke a given refresh token.
     */
    @Transactional
    public void revoke(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    @Transactional
    public void revokeByToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    /**
     * Delete expired tokens (e.g. scheduled cleanup).
     */
    @Transactional
    public int deleteExpired() {
        return refreshTokenRepository.deleteByExpiryDateBefore(Instant.now());
    }
}

