package com.certificate.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CollaborationRequestDTO {
    private Long id;
    private Long eventId;
    private String eventName;
    private String senderName;
    private String senderEmail;
    private String status;
    private LocalDateTime createdAt;
}
