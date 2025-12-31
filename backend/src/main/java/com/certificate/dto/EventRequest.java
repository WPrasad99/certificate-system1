package com.certificate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventRequest {
    private String eventName;
    private LocalDate eventDate;
    private String organizerName;
    private String instituteName;
}
