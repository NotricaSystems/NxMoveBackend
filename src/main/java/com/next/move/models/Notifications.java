package com.next.move.models;

import com.next.move.enums.NotifType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notifications {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userProfileId;
    private Long goalId;
    private String phone;
    @Column(nullable = false, updatable = false)
    private Instant sendingDate = Instant.now();  // default at creation
    @Column(length = 1000)
    private String notification;
    @Column(length = 1000)
    private String usersReply;
    @Column(length = 1000)
    private String replyBack;
    private Boolean browserNotified;
    //private NotifType notifType;
}
