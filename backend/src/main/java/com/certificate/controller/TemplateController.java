package com.certificate.controller;

import com.certificate.entity.Template;
import com.certificate.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/events/{eventId}/template")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadTemplate(
            @PathVariable Long eventId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            templateService.uploadTemplate(eventId, authentication.getName(), file);
            return ResponseEntity.ok("Template uploaded successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Failed to upload template");
        }
    }

    @GetMapping
    public ResponseEntity<Template> getTemplate(
            @PathVariable Long eventId,
            Authentication authentication) {
        Template template = templateService.getTemplate(eventId, authentication.getName());
        return ResponseEntity.ok(template);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteTemplate(
            @PathVariable Long eventId,
            Authentication authentication) {
        try {
            templateService.deleteTemplate(eventId, authentication.getName());
            return ResponseEntity.ok("Template deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
