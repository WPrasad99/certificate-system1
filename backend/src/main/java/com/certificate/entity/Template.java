package com.certificate.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "template_name")
    private String templateName;

    @Lob
    @Column(name = "image_data", columnDefinition = "BYTEA")
    private byte[] imageData;

    @Column(name = "is_default")
    private Boolean isDefault = false;

    // Coordinates for name placement
    @Column(name = "name_x")
    private Integer nameX = 512; // Default center X

    @Column(name = "name_y")
    private Integer nameY = 410; // Default Y for name

    @Column(name = "font_size")
    private Integer fontSize = 48;

    @Column(name = "font_color")
    private String fontColor = "#000000"; // Black

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
