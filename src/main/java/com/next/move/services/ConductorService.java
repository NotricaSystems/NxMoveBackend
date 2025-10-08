package com.next.move.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.next.move.enums.GoalStatus;
import com.next.move.models.Goals;
import com.next.move.models.Subtasks;
import com.next.move.models.UserProfile;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;

@Service
public class ConductorService {

    private final DataService dataService;

    public ConductorService(LoginService loginService, DataService dataService) {
        this.dataService = dataService;
    }

    public UserProfile updateUserProfile(UserProfile userProfile) {
        return this.dataService.updateUserProfile(prepareTheGoal(userProfile));
    }

    private UserProfile prepareTheGoal(UserProfile userProfile) {
        System.out.println("prepareTheNewGoal begining:");
        System.out.println(userProfile);
        for (Goals goal : userProfile.getGoalsList()) {
            goal.setUserProfile(userProfile);
            if (goal.getStartDate() == null && goal.getStatus() == GoalStatus.STARTED.getCode()) {
                goal.setStartDate(Instant.now());
            }
            for (Subtasks subtask : goal.getSubtasksList()) {
                subtask.setGoal(goal);
            }
            if (goal.getId() != null) {
                Goals currentGoal = dataService.getTheGoal(goal.getId());
                if (currentGoal.getRemainingSeconds() != null && goal.getRemainingSeconds() != null && goal.getRemainingSeconds() > currentGoal.getRemainingSeconds()) {
                    goal.setRemainingSeconds(currentGoal.getRemainingSeconds());
                }
                if (currentGoal.getStartDate() != null && goal.getStartDate() != null &&
                        goal.getStartDate().isBefore(currentGoal.getStartDate())) {
                    goal.setStartDate(currentGoal.getStartDate());
                }
                if (currentGoal.getNotified()) {
                    goal.setNotified(true);
                }
            }
        }
        return userProfile;
    }

    @Transactional
    public UserProfile pauseGoal(UserProfile userProfile) {
        for(Goals goal: userProfile.getGoalsList()) {
            long seconds = ChronoUnit.SECONDS.between(goal.getStartDate(), Instant.now());
            goal.setRemainingSeconds(goal.getRemainingSeconds() - (int) seconds);
            goal.setLastPausedDate(Instant.now());
            goal.setStatus(GoalStatus.PAUSED.getCode());
            return updateUserProfile(userProfile);
        }
        return null;
    }

    @Transactional
    public UserProfile restartGoal(UserProfile userProfile) {
        for(Goals goal: userProfile.getGoalsList()) {
            goal.setStartDate(Instant.now());
            goal.setStatus(GoalStatus.STARTED.getCode());
            return updateUserProfile(userProfile);
        }
        return null;
    }
}
