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
        return this.dataService.updateUserProfile(prepareTheNewGoal(userProfile));
    }

    /*
    private UserProfile followGeneralLoginWorkflow(UserProfile userProfile) {
        try {
            //checking if it's a new user then create a new user profile and ask for user's phone
            Optional<UserProfile> existedUserProfile = this.dataService.getUserProfile(userProfile.getEmail());
            UserProfile updatedUserProfile;
            if (existedUserProfile.isEmpty()) {
                updatedUserProfile = this.dataService.addNewUser(prepareTheNewGoal(userProfile));
            } else {
                //Copying user info
                userProfile.setId(existedUserProfile.map(UserProfile::getId).orElse(null));
                userProfile.setGivenName(existedUserProfile.map(UserProfile::getGivenName).orElse(null));
                userProfile.setPhone(existedUserProfile.map(UserProfile::getPhone).orElse(null));

                //Inserting the new goal
                updatedUserProfile = updateUserProfile(userProfile);
            }
            return updatedUserProfile;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
        // if user is new, or she doesn't have an active goal then
        // sending an object that consists of user's email, phone (empty or not) and a JWT token
        // otherwise sending an extra object that has her goad, subtasks and the estimated time
        // if user has entered a new goal while having an active goal she must be asked if she wants
        // to abort the previous mission and start a new one, or she wants the previous goal to get loaded
    }
*/

    private UserProfile prepareTheNewGoal(UserProfile userProfile) {
        System.out.println("prepareTheNewGoal begining:");
        System.out.println(userProfile);
        for (Goals goal : userProfile.getGoalsList()) {
            goal.setUserProfile(userProfile);
            //boolean phoneRequired = goal.getSmsNotif() || goal.getWhatsappNotif();
            //goal.setStatus(setInitialStatus(userProfile, phoneRequired));
            if (goal.getStartDate() == null && goal.getStatus() == GoalStatus.STARTED.getCode()) {
                goal.setStartDate(Instant.now());
            }
            for (Subtasks subtask : goal.getSubtasksList()) {
                subtask.setGoal(goal);
            }
        }
        //System.out.println("prepareTheNewGoal end:");
        //System.out.println(userProfile);
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

    /*
    private Integer setInitialStatus(UserProfile userProfile, boolean phoneRequired) {
        if (!phoneRequired || (userProfile.getPhone() != null && !userProfile.getPhone().isBlank())) {
            return GoalStatus.STARTED.getCode();
        }
        return GoalStatus.CREATED.getCode();
    }*/

}
