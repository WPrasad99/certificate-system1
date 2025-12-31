package com.certificate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateVerificationDTO {
    private boolean valid;
    private String participantName;
    private String eventName;
    private LocalDate eventDate;
    private String organizerName;
    private String instituteName;
    private LocalDateTime generatedAt;
    private String verificationId;
}
