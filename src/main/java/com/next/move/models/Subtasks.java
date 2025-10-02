package com.next.move.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.next.move.models.Goals;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subtasks {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer priority;
    private Integer percentage;
    private String title;
    @Column(length = 1000)
    private String description;

    @ManyToOne
    @JoinColumn(name = "goal_id", nullable = false)
    private Goals goal;
}
