package com.next.move.dto;

import com.next.move.models.UserProfile;

import java.util.List;

public record UserProfileDTO(Long id, String email, String password, String givenName, String phone,
                             Boolean phoneVerified, String phoneVerificationCode,
                             Integer subscriptionPlan, String subscriptionId, Integer subscriptionStatus, String lastStatusUpdate,
                             List<GoalsDTO> goalsList) {
    public static UserProfileDTO fromEntity(UserProfile userProfile) {
        return new UserProfileDTO(
                userProfile.getId(),
                userProfile.getEmail(),
                userProfile.getPassword(),
                userProfile.getGivenName(),
                userProfile.getPhone(),
                userProfile.getPhoneVerified(),
                userProfile.getPhoneVerificationCode(),
                userProfile.getSubscriptionPlan(),
                userProfile.getSubscriptionId(),
                userProfile.getSubscriptionStatus(),
                userProfile.getLastStatusUpdate() == null ? "" : userProfile.getLastStatusUpdate().toString(),
                userProfile.getGoalsList() == null ? List.of() :
                        userProfile.getGoalsList().stream()
                                .map(GoalsDTO::fromEntity)
                                .toList()
        );
    }
}
