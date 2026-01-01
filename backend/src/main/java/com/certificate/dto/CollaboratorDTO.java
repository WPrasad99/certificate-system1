package com.certificate.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CollaboratorDTO {
    private Long userId;
    private String name;
    private String email;
    private String role;
    private String status;
    private LocalDateTime addedAt;
}
