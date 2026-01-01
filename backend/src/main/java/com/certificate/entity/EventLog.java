package com.certificate.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "event_logs")
public class EventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private Long userId; // The user who performed the action

    @Column(nullable = false)
    private String action; // e.g., "UPLOAD_PARTICIPANTS", "GENERATE_CERTIFICATES"

    @Column(length = 1000)
    private String details;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
