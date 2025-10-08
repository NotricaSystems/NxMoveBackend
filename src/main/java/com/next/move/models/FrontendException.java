package com.next.move.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FrontendException {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private Instant incidentDate = Instant.now();  // default at creation

    @Lob
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}
