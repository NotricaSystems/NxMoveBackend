package com.next.move.services;

import com.next.move.enums.GoalStatus;
import com.next.move.enums.NotifType;
import com.next.move.models.Goals;
import com.next.move.models.Notifications;
import com.next.move.models.Subtasks;
import com.next.move.models.UserProfile;
import com.next.move.repository.GoalRepository;
import com.next.move.repository.NotificationRepository;
import com.next.move.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class DataService {

    private final UserRepository userRepository;
    private final GoalRepository goalRepository;
    private final NotificationRepository notificationRepository;

    public DataService(UserRepository userRepository,
                       GoalRepository goalRepository,
                       NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.goalRepository = goalRepository;
        this.notificationRepository = notificationRepository;
    }

    public List<UserProfile> getAllUsers() { return this.userRepository.findAll();}
    public Optional<UserProfile> getUserProfile(String email) {
        return this.userRepository.findByEmail(email);
    }

    public Optional<UserProfile> getUserProfile(Long id) {
        return this.userRepository.findById(id);
    }

    @Transactional
    public UserProfile addNewUser(UserProfile userProfile) {
        return this.userRepository.saveAndFlush(userProfile);
    }

    @Transactional
    public UserProfile updateUserProfile(UserProfile userProfile) {
        return this.userRepository.saveAndFlush(userProfile);
    }

    public List<Goals> retrieveActiveGoals() {
        return this.goalRepository.retrieveGoals(GoalStatus.STARTED.getCode());
    }

    @Transactional
    public void updateGoals(List<Goals> goals) {
        this.goalRepository.saveAllAndFlush(goals);
    }

    @Transactional
    public Goals updateGoal(Goals goal) {
        return this.goalRepository.saveAndFlush(goal);
    }

    @Transactional
    public Notifications updateNotifications(Notifications notifications) {
        return this.notificationRepository.saveAndFlush(notifications);
    }

    public Optional<Notifications> getTheLatestNotification(String phone) {
        System.out.println(Instant.now().minus(25, ChronoUnit.HOURS));
        System.out.println(phone);
        return this.notificationRepository
                .getTheLatestNotification(phone, Instant.now().minus(25, ChronoUnit.HOURS))
                .stream().findFirst();
    }

    public Goals getTheGoal(Long goalId) {
        return this.goalRepository.getReferenceById(goalId);
    }

    public Optional<Notifications> getTheLatestNotification(Goals goal) {
        Instant since = Instant.now().minus(15, ChronoUnit.MINUTES);
        Optional<Notifications> notification = this.notificationRepository
                .getTheLatestNotificationByGoalId(goal.getId(), since)
                .stream().findFirst();
        if (notification.isPresent()) {
            notification.ifPresent(n -> n.setBrowserNotified(true));
        }
        return notification;
    }

}
