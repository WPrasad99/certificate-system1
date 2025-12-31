package com.certificate.service;

import com.certificate.entity.Event;
import com.certificate.entity.Template;
import com.certificate.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final EventService eventService;

    @Transactional
    public void uploadTemplate(Long eventId, String email, MultipartFile file) throws IOException {
        Event event = eventService.getEventById(eventId, email);

        // Check if file is PNG (by extension or content type)
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();

        boolean isPngExtension = filename != null && filename.toLowerCase().endsWith(".png");

        if (!isPngExtension) {
            throw new IllegalArgumentException("Only PNG files are allowed. Please ensure the file ends with .png");
        }

        Template template = templateRepository.findByEventId(eventId)
                .orElse(new Template());

        template.setEventId(eventId);
        template.setTemplateName(file.getOriginalFilename());
        template.setImageData(file.getBytes());
        template.setIsDefault(false);
        // Default coordinates are set in entity constructor/defaults, but can be
        // updated here if we had UI for it

        templateRepository.save(template);
    }

    public Template getTemplate(Long eventId, String email) {
        eventService.getEventById(eventId, email); // Check access
        return templateRepository.findByEventId(eventId)
                .orElse(null); // Return null if not found (frontend expects null or error)
    }

    @Transactional
    public void deleteTemplate(Long eventId, String email) {
        eventService.getEventById(eventId, email); // Check access
        Template template = templateRepository.findByEventId(eventId)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        templateRepository.delete(template);
    }
}
