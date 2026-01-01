package com.certificate.controller;

import com.certificate.dto.*;
import com.certificate.service.CollaborationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CollaborationController {

    private final CollaborationService collaborationService;

    /**
     * Invite collaborator to event
     */
    @PostMapping("/events/{eventId}/collaborators/invite")
    public ResponseEntity<?> inviteCollaborator(
            @PathVariable Long eventId,
            @RequestBody CollaboratorInviteRequest request,
            Authentication authentication) {

        try {
            String email = authentication.getName();
            CollaborationRequestDTO response = collaborationService.inviteCollaborator(eventId, email, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to invite collaborator: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get pending collaboration requests for current user
     */
    @GetMapping("/collaboration/requests")
    public ResponseEntity<List<CollaborationRequestDTO>> getRequests(Authentication authentication) {
        String email = authentication.getName();
        List<CollaborationRequestDTO> requests = collaborationService.getRequestsForUser(email);

        return ResponseEntity.ok(requests);
    }

    /**
     * Accept collaboration request
     */
    @PostMapping("/collaboration/requests/{requestId}/accept")
    public ResponseEntity<?> acceptRequest(
            @PathVariable Long requestId,
            Authentication authentication) {

        try {
            String email = authentication.getName();
            collaborationService.acceptRequest(requestId, email);
            return ResponseEntity.ok(Map.of("message", "Request accepted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Decline collaboration request
     */
    @PostMapping("/collaboration/requests/{requestId}/decline")
    public ResponseEntity<?> declineRequest(
            @PathVariable Long requestId,
            Authentication authentication) {

        try {
            String email = authentication.getName();
            collaborationService.declineRequest(requestId, email);
            return ResponseEntity.ok(Map.of("message", "Request declined"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get collaborators for an event
     */
    @GetMapping("/events/{eventId}/collaborators")
    public ResponseEntity<List<CollaboratorDTO>> getCollaborators(@PathVariable Long eventId) {
        List<CollaboratorDTO> collaborators = collaborationService.getEventCollaborators(eventId);
        return ResponseEntity.ok(collaborators);
    }

    /**
     * Remove collaborator from event
     */
    @DeleteMapping("/events/{eventId}/collaborators/{userId}")
    public ResponseEntity<?> removeCollaborator(
            @PathVariable Long eventId,
            @PathVariable Long userId,
            Authentication authentication) {

        try {
            String email = authentication.getName();
            collaborationService.removeCollaborator(eventId, userId, email);
            return ResponseEntity.ok(Map.of("message", "Collaborator removed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Resend invitation
     */
    @PostMapping("/events/{eventId}/collaborators/{userId}/resend")
    public ResponseEntity<?> resendInvitation(
            @PathVariable Long eventId,
            @PathVariable Long userId,
            Authentication authentication) {

        try {
            String email = authentication.getName();
            collaborationService.resendInvitation(eventId, email, userId);
            return ResponseEntity.ok(Map.of("message", "Invitation resent successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Search users by email
     */
    @GetMapping("/users/search")
    public ResponseEntity<List<UserSearchDTO>> searchUsers(@RequestParam String email, Authentication authentication) {
        String currentUserEmail = authentication.getName();
        List<UserSearchDTO> users = collaborationService.searchUsers(email, currentUserEmail);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/events/{eventId}/collaborators/{userId}/logs")
    public ResponseEntity<?> getCollaboratorLogs(
            @PathVariable Long eventId,
            @PathVariable Long userId) {
        try {
            return ResponseEntity.ok(collaborationService.getCollaboratorLogs(eventId, userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/collaboration/sent-requests")
    public ResponseEntity<List<CollaborationRequestDTO>> getSentRequests(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(collaborationService.getSenderRequests(email));
    }
}
