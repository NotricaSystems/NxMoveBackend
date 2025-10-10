package com.next.move.controllers;

import com.next.move.dto.UserProfileDTO;
import com.next.move.models.UserProfile;
import com.next.move.repository.UserRepository;
import com.next.move.security.*;
import com.next.move.services.DataService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;

import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authManager;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final DataService dataService;

    public AuthController(
            AuthenticationManager authManager,
            JwtUtils jwtUtils,
            RefreshTokenService refreshTokenService,
            DataService dataService
    ) {
        this.authManager = authManager;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
        this.dataService = dataService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpServletResponse resp) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(auth);

        String accessToken = jwtUtils.generateAccessToken((UserDetails) auth.getPrincipal());

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        UserProfile user = userDetails.getUser();
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
        //RefreshToken refreshToken = refreshTokenService.createRefreshToken(((UserProfile)auth.getPrincipal()).getId());

        long maxAge = refreshToken.getExpiryDate().getEpochSecond() - Instant.now().getEpochSecond();

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken.getToken())
                .httpOnly(true).secure(true).path("/api/auth/refresh")
                .maxAge(maxAge).sameSite("None").build();
        resp.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        UserProfile userProfile = dataService.getUserProfile(req.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(new LoginResponse(accessToken, jwtUtils.getJwtExpirationMs(),
                UserProfileDTO.fromEntity(userProfile), GeneralLoginService.hasActiveGoal(user)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(name="refreshToken", required=false) String refreshTokenCookie,
                                     HttpServletResponse resp) {
        if (refreshTokenCookie == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        RefreshToken rt = refreshTokenService.verify(refreshTokenCookie);
        // rotate token:
        //refreshTokenService.revoke(rt);
        RefreshToken newRt = refreshTokenService.createRefreshToken(rt.getUser().getId());

        String accessToken = jwtUtils.generateAccessToken(new CustomUserDetails(rt.getUser()));
        long maxAge = newRt.getExpiryDate().getEpochSecond() - Instant.now().getEpochSecond();

        ResponseCookie cookie = ResponseCookie.from("refreshToken", newRt.getToken())
                .httpOnly(true).secure(true).path("/api/auth/refresh")
                .maxAge(maxAge).sameSite("None").build();
        resp.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(new AccessTokenResponse(accessToken, refreshTokenService.getRefreshTokenDurationMs()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(name="refreshToken", required=false) String refreshTokenCookie,
                                    HttpServletResponse resp) {
        if (refreshTokenCookie != null) refreshTokenService.revokeByToken(refreshTokenCookie);
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true).secure(true).path("/api/auth/refresh").maxAge(0).build();
        resp.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok().build();
    }
}

