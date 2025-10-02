package com.next.move.security;

import com.next.move.enums.GoalStatus;
import com.next.move.models.UserProfile;

public class GeneralLoginService {

    public static boolean hasActiveGoal(UserProfile user) {
        if (user.getGoalsList() != null && !user.getGoalsList().isEmpty()) {
            return user.getGoalsList().stream()
                    .anyMatch(g -> g.getStatus() == GoalStatus.STARTED.getCode() || g.getStatus() == GoalStatus.PAUSED.getCode());
        }
        return false;
    }
}
