package com.next.move.controllers;

import com.next.move.dto.GoalsDTO;
import com.next.move.dto.UserProfileDTO;
import com.next.move.enums.SubStatus;
import com.next.move.models.*;
import com.next.move.services.*;
import com.next.move.utilities.RandomPasswordGenerator;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class HubController {

    private static final Logger log = LoggerFactory.getLogger(HubController.class);
    private final ChatGptService chatGptService;
    private final ConductorService conductorService;
    private final PasswordEncoder passwordEncoder;
    private final PayPalService paypalService;
    private final DataService dataService;
    private final NotifierService notifierService;
    private final EmailService emailService;

    public HubController(ChatGptService chatGptService, ConductorService conductorService,
                         DataService dataService, PasswordEncoder passwordEncoder,
                         PayPalService paypalService, NotifierService notifierService, EmailService emailService) {
        this.conductorService = conductorService;
        this.chatGptService = chatGptService;
        this.dataService = dataService;
        this.passwordEncoder = passwordEncoder;
        this.paypalService = paypalService;
        this.notifierService = notifierService;
        this.emailService = emailService;
    }

    @GetMapping("/planner")
    ResponseEntity<Plan> goalPlanner(@RequestParam(value = "goal") String goal) throws Exception {
        try {
            return ResponseEntity.ok(chatGptService.planTheGoal(goal));
        } catch (Exception e) {
            log.error(e.getMessage());
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<UserProfileDTO> signup(@Valid @RequestBody UserProfile request) throws Exception {
        try {
            // If validation passes, you can safely save the user
            // Otherwise, Spring will automatically throw MethodArgumentNotValidException
            UserProfile newRegisteredUser = new UserProfile();
            Optional<UserProfile> userProfile = dataService.getUserProfile(request.getEmail());
            if (userProfile.isPresent()) {
                if (userProfile.get().getPassword() != null && !userProfile.get().getPassword().isBlank()) {
                    throw new Exception("A User is already registered with this email");
                } else {
                    newRegisteredUser.setId(userProfile.get().getId());
                }
            }
            newRegisteredUser.setEmail(request.getEmail());
            newRegisteredUser.setPhone(request.getPhone());
            newRegisteredUser.setGivenName(request.getGivenName());
            newRegisteredUser.setSubscriptionPlan(request.getSubscriptionPlan());
            newRegisteredUser.setSubscriptionStatus(request.getSubscriptionStatus());
            newRegisteredUser.setPassword(passwordEncoder.encode(request.getPassword()));
            newRegisteredUser = dataService.updateUserProfile(newRegisteredUser);

            return ResponseEntity.ok(UserProfileDTO.fromEntity(newRegisteredUser));
        } catch (Exception e) {
            log.error(e.getMessage());
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/receive")
    ResponseEntity<?> receiveResponse(@RequestParam Map<String, String> params) {
        try {
            // params contains all form fields
            String body = params.get("Body");
            String phone = params.get("From");

            System.out.println("text received: " + body);
            if (body.isBlank()) {
                return (ResponseEntity<?>) ResponseEntity.ok();
            } else {
                //Call a service to store the user's response and to generate an AI generated reply
                return ResponseEntity.ok("Message: " + notifierService.replyBack(phone, body));
            }
        } catch (Exception e) {
            //Modify this later to return a ResponseEntity
            log.error(e.getMessage());
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/update")
    ResponseEntity<UserProfileDTO> updateUserProfile(@RequestBody UserProfile userProfile) throws Exception {
        try {
            if (userProfile.getSubscriptionId() != null) {
                //Updating subscription status
                SubStatus subStatus = paypalService.getSubscriptionStatus(userProfile.getSubscriptionId());
                if (subStatus.getCode() != userProfile.getSubscriptionStatus()) {
                    userProfile.setSubscriptionStatus(subStatus.getCode());
                    userProfile.setLastStatusUpdate(Instant.now());
                }
            }
            if (userProfile.getPassword() != null && !userProfile.getPassword().isBlank()) {
                userProfile.setPassword(passwordEncoder.encode(userProfile.getPassword()));
            }
            System.out.println(userProfile);
            UserProfile updatedUserProfile = this.conductorService.updateUserProfile(userProfile);
            updatedUserProfile.setPassword("");
            return ResponseEntity.ok(UserProfileDTO.fromEntity(updatedUserProfile));
        } catch (Exception e) {
            log.error(e.getMessage());
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/userprofile")
    ResponseEntity<UserProfileDTO> retrieveUserProfile(@AuthenticationPrincipal UserDetails userDetails) throws Exception {
        try {
            UserProfile userProfileWithArchive = this.dataService.getUserProfile(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            userProfileWithArchive.setPassword("");
            log.info("user profile retrieved for user:" + userDetails.getUsername());
            System.out.println("sub plan: " + userProfileWithArchive.getSubscriptionPlan());
            return ResponseEntity.ok(UserProfileDTO.fromEntity(userProfileWithArchive));
        } catch (Exception e) {
            log.error(e.getMessage());
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/goal/update")
    ResponseEntity<GoalsDTO> updateGoal(@RequestBody Goals goal) throws Exception {
        try {
            Goals updatedGoal = dataService.updateGoal(goal);
            return ResponseEntity.ok(GoalsDTO.fromEntity(updatedGoal));
        } catch (Exception e) {
            log.error(e.getMessage());
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/paypal/unsubscribe")
    ResponseEntity<UserProfileDTO> unsubscribe(@RequestBody UserProfile userProfile) throws Exception {
        try {
            if (paypalService.cancelSubscription(userProfile.getSubscriptionId(), null)) {
                userProfile.setSubscriptionStatus(paypalService.getSubscriptionStatus(userProfile.getSubscriptionId()).getCode());
                dataService.updateUserProfile(userProfile);
                return ResponseEntity.ok(UserProfileDTO.fromEntity(userProfile));
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/notification")
    ResponseEntity<Notifications> getLatestNotification(@RequestBody Goals goal) throws Exception {
        try {
            Optional<Notifications> notifications = dataService.getTheLatestNotification(goal);
            return notifications.map(ResponseEntity::ok).orElse(null);
        } catch (Exception e) {
            log.error(e.getMessage());
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/forgotpassword")
    ResponseEntity<?> unsubscribe(@RequestBody String email) throws Exception {
        try {
            Optional<UserProfile> userProfile = dataService.getUserProfile(email);
            if (userProfile.isPresent()) {
                if (userProfile.get().getPassword() == null || userProfile.get().getPassword().isBlank()) {
                    return ResponseEntity
                            .status(HttpStatus.NOT_FOUND)
                            .body("It looks like you haven’t signed up yet. Please create an account to get started.");
                }
                String randomPassword = RandomPasswordGenerator.generate(8);
                emailService.sendPasswordResetEmail(email, randomPassword);
                userProfile.ifPresent(u -> u.setPassword(passwordEncoder.encode(randomPassword)));
                dataService.updateUserProfile(userProfile.get());
                return ResponseEntity.ok(Map.of("message", "We’ve sent a new temporary password to your email address."));
            } else {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body("No account is associated with this email address. Please verify your email or sign up to create an account.");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            System.out.println(e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Sorry, we can’t process your request at the moment. Please try again in a little while.");
        }
    }


    @PostMapping("/goal/pause")
    ResponseEntity<UserProfileDTO> pauseGoal(@RequestBody UserProfile userProfile) throws Exception {
        try {
            UserProfile updatedGoal = conductorService.pauseGoal(userProfile);
            return ResponseEntity.ok(UserProfileDTO.fromEntity(updatedGoal));
        } catch (Exception e) {
            log.error(e.getMessage());
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/goal/restart")
    ResponseEntity<UserProfileDTO> restartGoal(@RequestBody UserProfile userProfile) throws Exception {
        try {
            UserProfile updatedGoal = conductorService.restartGoal(userProfile);
            return ResponseEntity.ok(UserProfileDTO.fromEntity(updatedGoal));
        } catch (Exception e) {
            log.error(e.getMessage());
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/frontend/log")
    ResponseEntity<?> logFrontendError(@RequestBody String errorMessage) throws Exception {
        try {
            FrontendException frontendException = new FrontendException();
            frontendException.setErrorMessage(errorMessage);
            dataService.storeFrontendError(frontendException);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error(e.getMessage());
            System.out.println(e.getMessage());
            return null;
        }
    }

    //The endpoints below are just for test purposes
    /*
    @GetMapping("/query")
    String getChatResponse(@RequestParam(value = "message") String query) {
        return chatClient
                .prompt()                          // Start prompt building
                .user(query)                       // User message
                .call()                            // Send to model
                .content();                        // Get result as String
    }

    @GetMapping("/simulation")
    HttpEntity<Map<String, String>> getChatSimulationResponse(@RequestParam(value = "message") String query) throws Exception {
        //twilioService.sendSms("first text from the api (Farid)");
        //Thread.sleep(3000);
        paypalService.getAccessToken();
        Map<String, String> response = Map.of("response", "This is a test response");
        return ResponseEntity.ok(response);  // 200 + JSON body
    }
*/


}
