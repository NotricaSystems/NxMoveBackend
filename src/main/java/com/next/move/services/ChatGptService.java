package com.next.move.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.next.move.controllers.HubController;
import com.next.move.models.Goals;
import com.next.move.models.Notifications;
import com.next.move.models.Plan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ChatGptService {

    private final ChatClient chatClient;
    private static final Logger log = LoggerFactory.getLogger(HubController.class);

    private static final String[] intensity = {
            "Calm and respectful",
            "Polite and warm",
            "Playful and non-intrusive",
            "Energetic and upbeat",
            "Pushy and slightly cheeky",
            "Rude-humor tone",
            "Aggressive and vulgar"
    };

    public ChatGptService(ChatClient.Builder builder) {
        this.chatClient = builder
                .build();
    }

    public String askChatGpt(String prompt) {
        String plan = null;
        try {
            plan = chatClient
                    .prompt()                          // Start prompt building
                    .user(prompt)                       // User message
                    .call()                            // Send to model
                    .content();

            return plan;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    private String goalPlannerPrompt(String goal) {
        String prompt = "I have a goal and it’s " + goal.trim() + ". ";

        String content = "Please give me a time estimate (in days and hours only) to achieve this goal. " +
                "AI generate notification texts needs to be sent later to check the user's progress or for guiding " +
                "them through the process, please give me the frequency of these notifications in minute. " +
                "Determine the frequency based on the importance and the nature of the goal. " +
                "And also break it down to maximum three essential sub tasks that you thing finishing these task will get me to the goal." +
                "Each subtask must have a main sentence (at least two words). Also please provide at least 3 description sentences for each subtask. " +
                "At the beginning of each subtask, should be a percentage indicating the amount of total time should be spent on each of them. " +
                "Each task must have a priority or order number (1 to 3). ";

        String format = "Please format your answer as a JSON object. " +
                "The root of the object should have two items: \"estimatedTime\" and \"subtasksList\". " +
                "\"estimatedTime\" must have three fields: \"days\" and \"hours\", and the notification frequency " +
                "as \"frequency\" they all have values as numbers. " +
                "The subtasksList must be an array of maximum three subtasks, each containing \"priority\" and \"percentage\" as numbers " +
                "and \"title\" and \"description\" as string for each subtask. Also each subtask must have a \"id\" property with null value. " +
                "Please prevent any of these characters in your response: < > " +
                "and please do not put \",\" at the end of the last property value of any of the JSON objects.";

        prompt += content + format;

        return prompt;
    }

    public Plan planTheGoal(String goal) throws JsonProcessingException {
        String plan = askChatGpt(goalPlannerPrompt(goal));
        ObjectMapper mapper = new ObjectMapper().enable(JsonReadFeature.ALLOW_TRAILING_COMMA.mappedFeature());
        //Modify this later to return a ResponseEntity
        return mapper.readValue(plan, Plan.class);
    }

    public String aiNotifGenerator(Goals goal, boolean timeIsUp) {
        //If the OpenAI didn't respond, or it delayed for a long time, I can send and static message
        String prompt = "Assume that you are acting as a supportive mentor or friend. " +
                "A user has set  \"" + goal.getTitle() + "\" as their goal or routine, " +
                "and wants to achieve/complete it within remaining time " + remainingTime(goal) +
                ". You have agreed to send encouraging or reminding messages every " + notificationFreq(goal) +
                ". Write a motivational (if it's a goal) or a reminder message (if it's a routine) of around 3 sentences " +
                "and it must be less than 100 characters, directly addressing the mentee. " +
                "The tone must be " + intensity[goal.getIntensity()] + " and  match whether it’s a goal (focused on achievement) or a routine (focused on consistency). " +
                "The text shouldn't have any format, just plain text.";

        System.out.println(prompt);
        return askChatGpt(prompt);
    }

    public String aiReplyBackGenerator(Goals goal, Notifications latestNotification, String usersMessage) {
        String prompt = "Assume that you are acting as a supportive mentor or friend. " +
                "A user has set  \"" + goal.getTitle() + "\" as their goal or routine, " +
                "and wants to achieve/complete it within remaining time " + remainingTime(goal) +
                ". You have agreed to send encouraging or reminding messages every " + notificationFreq(goal) +
                " The person has now replied to your last message, and it is: \"" + usersMessage +
                "\". In your response, directly acknowledge their reply and continue the conversation naturally. " +
                "If their reply shows struggles, doubts, or excuses, motivate them to take action and highlight " +
                " something like there is always time, solutions exist, priorities matter, and excuses hold them back " +
                "(choose the most fitting point, not all). " +
                "If their reply is positive and shows progress, reinforce their momentum, " +
                "celebrate their wins, and encourage them to keep going strong. " +
                "Write a reply for fewer than 100 characters, directly addressing the sender. " +
                "The tone must be " + intensity[goal.getIntensity()] + " and  match whether it’s a goal (focused on achievement) or a routine (focused on consistency). " +
                "The text shouldn't have any format, just plain text.";

        System.out.println(prompt);
        return askChatGpt(prompt);
    }

    private String remainingTime(Goals goal) {
        long totalSeconds = goal.getRemainingSeconds();

        long days = totalSeconds / (24 * 3600);
        totalSeconds %= (24 * 3600);
        long hours = totalSeconds / 3600;
        totalSeconds %= 3600;
        long minutes = totalSeconds / 60;

        String estimate = "";
        if (days > 0) {
            estimate += days + " days";
        }
        if (hours > 0) {
            if (!estimate.isBlank()) estimate += " and ";
            estimate += hours + " hours";
        }
        if (minutes > 0) {
            if (!estimate.isBlank()) estimate += " and ";
            estimate += minutes + " minutes";
        }
        return estimate;
    }

    private String notificationFreq(Goals goal) {
        String notifFreq = "";
        int hours = goal.getFrequency() / 60;
        int minutes = goal.getFrequency() % 60;
        if (hours > 0) {
            notifFreq += hours + " hours";
        }
        if (minutes > 0) {
            if (hours > 0) notifFreq += " and ";
            notifFreq += minutes + " minutes";
        }
        return notifFreq;
    }

}
