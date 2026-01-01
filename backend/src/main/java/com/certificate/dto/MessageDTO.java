package com.certificate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private Long id;
    private Long eventId;
    private Long senderId;
    private Long receiverId;
    private String senderName;
    private String content;
    private LocalDateTime timestamp;
    private boolean isRead;
}
