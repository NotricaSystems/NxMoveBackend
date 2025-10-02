package com.next.move.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Goals {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private Integer estimatedDays;
    private Integer estimatedHours;
    private Boolean browserNotif;

    private Boolean smsNotif; //just one of these two options are allowed
    private Boolean whatsappNotif;

    private Integer intensity; //accepts 1 to 7
    private Integer frequency; //in minutes

    private Integer status; // 0:just created 1:started 2:paused 3:overdue 4:achieved 5:aborted

    @Column(nullable = false, updatable = false)
    private Instant createdDate = Instant.now();  // default at creation

    private Instant lastPausedDate;
    private Instant startDate;
    private Integer remainingSeconds;

    private Boolean notified = false;

    @ManyToOne
    @JoinColumn(name = "userProfile_id", nullable = false)
    private UserProfile userProfile;

    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.EAGER)
    private List<Subtasks> subtasksList;
}
