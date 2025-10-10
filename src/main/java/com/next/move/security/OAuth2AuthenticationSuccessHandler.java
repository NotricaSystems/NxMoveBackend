package com.next.move.security;

import com.next.move.enums.GoalStatus;
import com.next.move.enums.SubPlan;
import com.next.move.enums.SubStatus;
import com.next.move.models.UserProfile;
import com.next.move.repository.UserRepository;
import com.next.move.services.DataService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final DataService dataService;

    @Value("${backend.domain}")
    private String backendDomain;

    public OAuth2AuthenticationSuccessHandler(JwtUtils jwtUtils,
                                              RefreshTokenService refreshTokenService,
                                              DataService dataService) {
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
        this.dataService = dataService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        System.out.println("Auth2 authentication success...");

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // Example: extract email from Google/Facebook profile
        String email = (String) oAuth2User.getAttributes().get("email");

        // find or create a User in your DB
        UserProfile user = dataService.getUserProfile(email)
                .orElseGet(() -> {
                    UserProfile newUser = new UserProfile();
                    newUser.setEmail(email);
                    newUser.setSubscriptionPlan(SubPlan.FREE.getCode());
                    newUser.setSubscriptionStatus(SubStatus.ACTIVE.getCode());
                    newUser.setPassword(""); // not used for OAuth2 accounts
                    return dataService.addNewUser(newUser);
                });

        System.out.println("user is: " +  user.getEmail());
        System.out.println("password is: " +  user.getPassword());
        System.out.println(List.of(new SimpleGrantedAuthority("ROLE_USER")));

        if (user.getPassword() == null) {
            user.setPassword("");
        }

        // Generate access + refresh tokens
        String accessToken = jwtUtils.generateAccessToken(
                new org.springframework.security.core.userdetails.User(
                        user.getEmail(),
                        user.getPassword(),
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        // set refresh token cookie
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken.getToken())
                .httpOnly(true).secure(true).path("/api/auth/refresh")
                .maxAge(refreshToken.getExpiryDate().getEpochSecond() - Instant.now().getEpochSecond())
                .sameSite("None").domain(backendDomain)  // allow both nx-move.com and api.nx-move.com
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        response.setContentType("text/html");
        response.getWriter().write(
                "<script>" +
                        "window.opener.postMessage(" +
                        "{accessToken: '" + accessToken +
                        "', hasActiveGoal: " +  GeneralLoginService.hasActiveGoal(user) +
                        ", userProfile: {id: " + user.getId() +
                        ", email: '" + user.getEmail() + "'" +
                        (user.getPhone() != null ? ", phone: '" + user.getPhone() + "'" : "") +
                        (user.getSubscriptionPlan() != null ? ", subscriptionPlan: " + user.getSubscriptionPlan() : "") +
                        (user.getSubscriptionId() != null ? ", subscriptionId: " + user.getSubscriptionId() : "") +
                        (user.getSubscriptionStatus() != null ? ", subscriptionStatus: " + user.getSubscriptionStatus() : "") +
                        (user.getLastStatusUpdate() != null ? ", lastStatusUpdate: '" + user.getLastStatusUpdate() + "'" : "") +
                        (user.getGivenName() != null ? ", givenName: '" + user.getGivenName() + "'" : "") +
                        "} }," +
                        " '*');" +
                        "window.close();" +
                        "</script>"
        );

    }


}

