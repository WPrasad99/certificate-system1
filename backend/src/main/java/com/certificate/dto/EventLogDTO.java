package com.certificate.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EventLogDTO {
    private Long id;
    private Long eventId;
    private Long userId;
    private String userName;
    private String action;
    private String details;
    private LocalDateTime timestamp;
}
