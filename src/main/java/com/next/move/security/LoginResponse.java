package com.next.move.security;

import com.next.move.dto.UserProfileDTO;
import lombok.Getter;

@Getter
public class LoginResponse {
    private String accessToken;
    private long expiresIn;
    private UserProfileDTO userProfile;
    private boolean hasActiveGoal;

    public LoginResponse(String accessToken, long expiresIn,
                         UserProfileDTO userProfileDTO, boolean hasActiveGoal) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.userProfile = userProfileDTO;
        this.hasActiveGoal = hasActiveGoal;
    }
}
