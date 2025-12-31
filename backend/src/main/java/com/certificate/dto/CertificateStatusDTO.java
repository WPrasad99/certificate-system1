package com.certificate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateStatusDTO {
    private Long id;
    private String participantName;
    private String email;
    private String generationStatus;
    private String emailStatus;
    private String updateEmailStatus;
    private String errorMessage;
}
