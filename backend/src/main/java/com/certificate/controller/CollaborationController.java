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
@RequestMapping("/api")
@RequiredArgsConstructor
public class CollaborationController {

    private final CollaborationService collaborationService;

    /**
     * Invite collaborator to event
     */
    @PostMapping("/events/{eventId}/collaborators/invite")
    public ResponseEntity<CollaborationRequestDTO> inviteCollaborator(
            @PathVariable Long eventId,
            @RequestBody CollaboratorInviteRequest request,
            Authentication authentication) {

        String email = authentication.getName();
        CollaborationRequestDTO response = collaborationService.inviteCollaborator(eventId, email, request);

        return ResponseEntity.ok(response);
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
    public ResponseEntity<Map<String, String>> acceptRequest(
            @PathVariable Long requestId,
            Authentication authentication) {

        String email = authentication.getName();
        collaborationService.acceptRequest(requestId, email);

        return ResponseEntity.ok(Map.of("message", "Request accepted successfully"));
    }

    /**
     * Decline collaboration request
     */
    @PostMapping("/collaboration/requests/{requestId}/decline")
    public ResponseEntity<Map<String, String>> declineRequest(
            @PathVariable Long requestId,
            Authentication authentication) {

        String email = authentication.getName();
        collaborationService.declineRequest(requestId, email);

        return ResponseEntity.ok(Map.of("message", "Request declined"));
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
    public ResponseEntity<Map<String, String>> removeCollaborator(
            @PathVariable Long eventId,
            @PathVariable Long userId,
            Authentication authentication) {

        String email = authentication.getName();
        collaborationService.removeCollaborator(eventId, userId, email);

        return ResponseEntity.ok(Map.of("message", "Collaborator removed successfully"));
    }

    /**
     * Search users by email
     */
    @GetMapping("/users/search")
    public ResponseEntity<List<UserSearchDTO>> searchUsers(@RequestParam String email) {
        List<UserSearchDTO> users = collaborationService.searchUsers(email);
        return ResponseEntity.ok(users);
    }
}
