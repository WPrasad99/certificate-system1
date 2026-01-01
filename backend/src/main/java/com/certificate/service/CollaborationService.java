package com.certificate.service;

import com.certificate.dto.*;
import com.certificate.entity.*;
import com.certificate.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollaborationService {

    private final CollaborationRequestRepository requestRepository;
    private final EventCollaboratorRepository collaboratorRepository;
    private final OrganizerRepository organizerRepository;
    private final EventRepository eventRepository;

    /**
     * Send collaboration invitation
     */
    @Transactional
    public CollaborationRequestDTO inviteCollaborator(Long eventId, String senderEmail,
            CollaboratorInviteRequest request) {
        // Validate event exists
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // Validate sender is the owner
        Organizer sender = organizerRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        if (!event.getOrganizerEmail().equals(senderEmail)) {
            throw new RuntimeException("Only event owner can invite collaborators");
        }

        // Validate recipient exists
        Organizer recipient = organizerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getEmail()));

        // Prevent self-invitation
        if (sender.getId().equals(recipient.getId())) {
            throw new RuntimeException("You cannot invite yourself");
        }

        // Check if already a collaborator
        if (collaboratorRepository.existsByEventIdAndUserId(eventId, recipient.getId())) {
            throw new RuntimeException("User is already a collaborator on this event");
        }

        // Check for pending invitation
        if (requestRepository.existsByEventIdAndRecipientEmailAndStatus(eventId, request.getEmail(), "PENDING")) {
            throw new RuntimeException("Invitation already sent to this user");
        }

        // Create invitation request
        CollaborationRequest collab = new CollaborationRequest();
        collab.setEventId(eventId);
        collab.setSenderId(sender.getId());
        collab.setRecipientEmail(request.getEmail());
        collab.setStatus("PENDING");

        CollaborationRequest saved = requestRepository.save(collab);

        log.info("Collaboration invitation sent: Event={}, Sender={}, Recipient={}",
                eventId, senderEmail, request.getEmail());

        // Map to DTO
        CollaborationRequestDTO dto = new CollaborationRequestDTO();
        dto.setId(saved.getId());
        dto.setEventId(saved.getEventId());
        dto.setEventName(event.getEventName());
        dto.setSenderName(sender.getFullName());
        dto.setSenderEmail(sender.getEmail());
        dto.setStatus(saved.getStatus());
        dto.setCreatedAt(saved.getCreatedAt());

        return dto;
    }

    /**
     * Get all pending requests for a user
     */
    public List<CollaborationRequestDTO> getRequestsForUser(String email) {
        List<CollaborationRequest> requests = requestRepository.findByRecipientEmailAndStatus(email, "PENDING");

        return requests.stream().map(req -> {
            Event event = eventRepository.findById(req.getEventId()).orElse(null);
            Organizer sender = organizerRepository.findById(req.getSenderId()).orElse(null);

            CollaborationRequestDTO dto = new CollaborationRequestDTO();
            dto.setId(req.getId());
            dto.setEventId(req.getEventId());
            dto.setEventName(event != null ? event.getEventName() : "Unknown Event");
            dto.setSenderName(sender != null ? sender.getFullName() : "Unknown");
            dto.setSenderEmail(sender != null ? sender.getEmail() : "");
            dto.setStatus(req.getStatus());
            dto.setCreatedAt(req.getCreatedAt());

            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * Accept collaboration request
     */
    @Transactional
    public void acceptRequest(Long requestId, String userEmail) {
        CollaborationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        // Validate recipient
        if (!request.getRecipientEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized");
        }

        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("Request already processed");
        }

        // Get user ID
        Organizer user = organizerRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create collaborator entry
        EventCollaborator collaborator = new EventCollaborator();
        collaborator.setEventId(request.getEventId());
        collaborator.setUserId(user.getId());
        collaborator.setRole("COLLABORATOR");
        collaboratorRepository.save(collaborator);

        // Update request status
        request.setStatus("ACCEPTED");
        request.setRespondedAt(LocalDateTime.now());
        requestRepository.save(request);

        log.info("Collaboration request accepted: RequestId={}, User={}", requestId, userEmail);
    }

    /**
     * Decline collaboration request
     */
    @Transactional
    public void declineRequest(Long requestId, String userEmail) {
        CollaborationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        // Validate recipient
        if (!request.getRecipientEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized");
        }

        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("Request already processed");
        }

        // Update request status
        request.setStatus("DECLINED");
        request.setRespondedAt(LocalDateTime.now());
        requestRepository.save(request);

        log.info("Collaboration request declined: RequestId={}, User={}", requestId, userEmail);
    }

    /**
     * Get collaborators for an event
     */
    public List<CollaboratorDTO> getEventCollaborators(Long eventId) {
        List<EventCollaborator> collaborators = collaboratorRepository.findByEventId(eventId);

        return collaborators.stream().map(collab -> {
            Organizer user = organizerRepository.findById(collab.getUserId()).orElse(null);

            CollaboratorDTO dto = new CollaboratorDTO();
            dto.setUserId(collab.getUserId());
            dto.setName(user != null ? user.getFullName() : "Unknown");
            dto.setEmail(user != null ? user.getEmail() : "");
            dto.setRole(collab.getRole());
            dto.setAddedAt(collab.getAddedAt());

            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * Remove collaborator
     */
    @Transactional
    public void removeCollaborator(Long eventId, Long userId, String ownerEmail) {
        // Validate event and ownership
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (!event.getOrganizerEmail().equals(ownerEmail)) {
            throw new RuntimeException("Only event owner can remove collaborators");
        }

        collaboratorRepository.deleteByEventIdAndUserId(eventId, userId);

        log.info("Collaborator removed: Event={}, UserId={}", eventId, userId);
    }

    /**
     * Search users by email
     */
    public List<UserSearchDTO> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        // Search by email (case-insensitive partial match)
        List<Organizer> users = organizerRepository.findAll().stream()
                .filter(org -> org.getEmail().toLowerCase().contains(query.toLowerCase()))
                .limit(10)
                .collect(Collectors.toList());

        return users.stream().map(user -> {
            UserSearchDTO dto = new UserSearchDTO();
            dto.setId(user.getId());
            dto.setName(user.getFullName());
            dto.setEmail(user.getEmail());
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * Check if user is collaborator on event
     */
    public boolean isCollaborator(Long eventId, String userEmail) {
        Organizer user = organizerRepository.findByEmail(userEmail).orElse(null);
        if (user == null)
            return false;

        return collaboratorRepository.existsByEventIdAndUserId(eventId, user.getId());
    }

    /**
     * Get all events user collaborates on
     */
    public List<Long> getCollaboratedEventIds(String userEmail) {
        Organizer user = organizerRepository.findByEmail(userEmail).orElse(null);
        if (user == null)
            return List.of();

        return collaboratorRepository.findEventIdsByUserId(user.getId());
    }
}
