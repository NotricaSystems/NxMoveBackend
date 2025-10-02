package com.next.move.services;

import com.next.move.enums.GoalStatus;
import com.next.move.enums.NotifType;
import com.next.move.enums.SubPlan;
import com.next.move.enums.SubStatus;
import com.next.move.models.Goals;
import com.next.move.models.Notifications;
import com.next.move.models.UserProfile;
import com.next.move.utilities.DateUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class NotifierService {

    private final DataService dataService;
    private final TwilioService twilioService;
    private final ChatGptService chatGptService;
    private final ConductorService conductorService;

    public NotifierService(DataService dataService,
                           TwilioService twilioService,
                            ChatGptService chatGptService,
                            ConductorService conductorService) {
        this.dataService = dataService;
        this.twilioService = twilioService;
        this.chatGptService = chatGptService;
        this.conductorService = conductorService;
    }

    public String replyBack(String phoneNumber, String usersMessage) {
        Optional<Notifications> latestNotification =  dataService.getTheLatestNotification(phoneNumber);
        System.out.println("replying back ");
        System.out.println(latestNotification);

        if (latestNotification.isPresent()) {
            //I need to fetch the goal to generate a better prompt
            Goals goal = dataService.getTheGoal(latestNotification.get().getGoalId());
            String replyBack = chatGptService.aiReplyBackGenerator(goal, latestNotification.get(), usersMessage);
            latestNotification.ifPresent(n -> {
                n.setUsersReply(usersMessage);
                n.setReplyBack(replyBack);
            });
            dataService.updateNotifications(latestNotification.get());
            return replyBack;
        }
        return null;
    }

    // Alternative: run at the start of each minute
    //@Scheduled(cron = "0 * * * * *") // second, minute, hour, day, month, weekday
    // Run every minute
    //@Scheduled(fixedRate = 60_000) // every 60 seconds from start
    public void notifier() {
        System.out.println("Notifier at work...");
        List<Goals> activeGoals = this.dataService.retrieveActiveGoals();
        String notifText = "";
        for (Goals goal : activeGoals) {
            try {
                long seconds = ChronoUnit.SECONDS.between(goal.getStartDate(), Instant.now());
                if (!goal.getNotified() || seconds >= (60L * goal.getFrequency())) {
                    goal.setStartDate(Instant.now());
                    goal.setRemainingSeconds(goal.getRemainingSeconds() - (int) seconds);
                    goal.setNotified(true);
                    boolean timeIsUp = false;
                    if (goal.getRemainingSeconds() <= 0) {
                        //time is up
                        goal.setRemainingSeconds(0);
                        timeIsUp = true;
                    }
                    this.dataService.updateGoal(goal);
                    if (askedForText(goal)) {
                        notifText = chatGptService.aiNotifGenerator(goal, timeIsUp);
                        twilioService.sendText(goal.getUserProfile().getPhone(), notifText, goal);
                    }
                    updateNotifications(goal, notifText);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
    private boolean askedForText(Goals goal) {
        return goal.getSmsNotif() || goal.getWhatsappNotif();
    }

    private void updateNotifications(Goals goal, String notifText) {
        Notifications notification = new Notifications();
        notifText = notifText.length() > 1000 ? notifText.substring(0,1000) : notifText;
        notification.setNotification(notifText);
        notification.setUserProfileId(goal.getUserProfile().getId());
        notification.setGoalId(goal.getId());
        //notification.setNotifType(NotifType.TEXT);
        notification.setPhone(goal.getUserProfile().getPhone());
        notification = dataService.updateNotifications(notification);
    }


    // Run every day at 8 pm
    @Scheduled(cron = "0 18 18 * * ?")
    public void subscriptionStatusUpdate() {
        System.out.println("Updating status task at work...");
        List<Goals> activeGoals = this.dataService.retrieveActiveGoals();
        for(Goals goal:activeGoals) {
            Integer subPlan = goal.getUserProfile().getSubscriptionPlan();
            Instant lastStatusUpdate = goal.getUserProfile().getLastStatusUpdate();
            if (subPlan == SubPlan.FREE.getCode()) {
                if (DateUtils.isOlderThanXDays(lastStatusUpdate, 8)) {
                    goal.setStatus(GoalStatus.PAUSED.getCode());
                    goal.getUserProfile().setSubscriptionStatus(SubStatus.EXPIRED.getCode());
                    goal.getUserProfile().setLastStatusUpdate(Instant.now());
                    this.conductorService.pauseGoal(goal.getUserProfile());
                    //Notifying user...
                    twilioService.sendText(goal.getUserProfile().getPhone(), subscriptionExpiredNotif(goal), goal);
                }
            } else if (goal.getUserProfile().getSubscriptionStatus() != SubStatus.ACTIVE.getCode()) {
                if (DateUtils.isOlderThanXDays(lastStatusUpdate, 31)) {
                    goal.setStatus(GoalStatus.PAUSED.getCode());
                    this.conductorService.pauseGoal(goal.getUserProfile());
                    //Notifying user...
                    twilioService.sendText(goal.getUserProfile().getPhone(), subscriptionExpiredNotif(goal), goal);
                }
            }
        }
    }

    private String subscriptionExpiredNotif(Goals goal) {
        int subPlan = goal.getUserProfile().getSubscriptionPlan();
        if (subPlan == SubPlan.FREE.getCode()) {
            return "Your free subscription has ended. Upgrade today at nxmove.ai to keep progressing with your goals and routines on NxMove!";
        } else {
            return "Your subscription has ended. Update your subscription today at nxmove.ai to keep progressing with your goals and routines on NxMove!";
        }
    }
}
