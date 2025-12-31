package com.certificate.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "certificates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "verification_id", unique = true)
    private String verificationId;

    @Column(name = "participant_id", nullable = false)
    private Long participantId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "generation_status")
    private String generationStatus = "PENDING";

    @Column(name = "email_status")
    private String emailStatus = "NOT_SENT";

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "email_sent_at")
    private LocalDateTime emailSentAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
