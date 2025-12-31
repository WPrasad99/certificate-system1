package com.certificate.controller;

import com.certificate.dto.ParticipantDTO;
import com.certificate.service.ParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/events/{eventId}/participants")
@RequiredArgsConstructor
public class ParticipantController {

    private final ParticipantService participantService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadParticipants(
            @PathVariable Long eventId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            List<ParticipantDTO> participants = participantService.uploadParticipants(eventId, file,
                    authentication.getName());
            return ResponseEntity.ok(participants);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getParticipants(@PathVariable Long eventId, Authentication authentication) {
        try {
            List<ParticipantDTO> participants = participantService.getParticipantsByEvent(eventId,
                    authentication.getName());
            return ResponseEntity.ok(participants);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{participantId}")
    public ResponseEntity<?> deleteParticipant(@PathVariable Long participantId, Authentication authentication) {
        try {
            participantService.deleteParticipant(participantId, authentication.getName());
            return ResponseEntity.ok(createSuccessResponse("Participant deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/all")
    public ResponseEntity<?> deleteAllParticipants(@PathVariable Long eventId, Authentication authentication) {
        try {
            participantService.deleteAllParticipants(eventId, authentication.getName());
            return ResponseEntity.ok(createSuccessResponse("All participants deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return response;
    }

    private Map<String, String> createSuccessResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }
}
