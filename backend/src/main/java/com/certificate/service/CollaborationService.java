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
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollaborationService {

    private final CollaborationRequestRepository requestRepository;
    private final EventCollaboratorRepository collaboratorRepository;
    private final OrganizerRepository organizerRepository;
    private final EventRepository eventRepository;
    private final EventLogRepository eventLogRepository;

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

        if (!event.getOrganizerId().equals(sender.getId())) {
            throw new RuntimeException("Only event owner can invite collaborators");
        }

        // Validate recipient exists
        Organizer recipient = organizerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getEmail()));

        // Prevent self-invitation
        if (sender.getId().equals(recipient.getId())) {
            throw new RuntimeException("You cannot invite yourself");
        }

        // Check if already an active collaborator (ACCEPTED)
        Optional<EventCollaborator> existingCollab = collaboratorRepository.findByEventIdAndUserId(eventId,
                recipient.getId());
        if (existingCollab.isPresent() && "ACCEPTED".equals(existingCollab.get().getStatus())) {
            throw new RuntimeException("User is already a collaborator on this event");
        }

        // Check for pending invitation (PENDING)
        if (requestRepository.existsByEventIdAndRecipientEmailAndStatus(eventId, request.getEmail(), "PENDING")) {
            throw new RuntimeException("already sent request pending");
        }

        // Create or Update collaborator entry as PENDING
        EventCollaborator collaborator = existingCollab.orElse(new EventCollaborator());
        collaborator.setEventId(eventId);
        collaborator.setUserId(recipient.getId());
        collaborator.setRole("COLLABORATOR");
        collaborator.setStatus("PENDING");
        collaboratorRepository.save(collaborator);

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
     * Get all requests sent by a user that are NOT pending (ACCEPTED/DECLINED)
     */
    public List<CollaborationRequestDTO> getSenderRequests(String userEmail) {
        Organizer sender = organizerRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CollaborationRequest> requests = requestRepository.findBySenderIdAndStatusNot(sender.getId(), "PENDING");

        return requests.stream().map(req -> {
            Event event = eventRepository.findById(req.getEventId()).orElse(null);
            Organizer recipient = organizerRepository.findByEmail(req.getRecipientEmail()).orElse(null);

            CollaborationRequestDTO dto = new CollaborationRequestDTO();
            dto.setId(req.getId());
            dto.setEventId(req.getEventId());
            dto.setEventName(event != null ? event.getEventName() : "Unknown Event");
            dto.setSenderName(recipient != null ? recipient.getFullName() : req.getRecipientEmail());
            dto.setSenderEmail(req.getRecipientEmail());
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

        // Find and update collaborator entry
        EventCollaborator collaborator = collaboratorRepository
                .findByEventIdAndUserId(request.getEventId(), user.getId())
                .orElse(new EventCollaborator());

        collaborator.setEventId(request.getEventId());
        collaborator.setUserId(user.getId());
        collaborator.setRole("COLLABORATOR");
        collaborator.setStatus("ACCEPTED");
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

        // Update collaborator entry to DECLINED if it exists
        Organizer user = organizerRepository.findByEmail(userEmail).orElse(null);
        if (user != null) {
            collaboratorRepository.findByEventIdAndUserId(request.getEventId(), user.getId())
                    .ifPresent(collab -> {
                        collab.setStatus("DECLINED");
                        collaboratorRepository.save(collab);
                    });
        }

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
            dto.setStatus(collab.getStatus());
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

        Organizer owner = organizerRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!event.getOrganizerId().equals(owner.getId())) {
            throw new RuntimeException("Only event owner can remove collaborators");
        }

        collaboratorRepository.deleteByEventIdAndUserId(eventId, userId);

        log.info("Collaborator removed: Event={}, UserId={}", eventId, userId);
    }

    /**
     * Resend invitation to a previously declined collaborator
     */
    @Transactional
    public void resendInvitation(Long eventId, String senderEmail, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        Organizer sender = organizerRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        Organizer recipient = organizerRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        // Only owner can resend
        if (!event.getOrganizerId().equals(sender.getId())) {
            throw new RuntimeException("Only event owner can manage collaborators");
        }

        // Update EventCollaborator status to PENDING
        EventCollaborator collaborator = collaboratorRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new RuntimeException("Collaborator entry not found"));

        collaborator.setStatus("PENDING");
        collaboratorRepository.save(collaborator);

        // Create a new PENDING request
        CollaborationRequest request = new CollaborationRequest();
        request.setEventId(eventId);
        request.setSenderId(sender.getId());
        request.setRecipientEmail(recipient.getEmail());
        request.setStatus("PENDING");
        requestRepository.save(request);

        log.info("Invitation resent: Event={}, Sender={}, Recipient={}",
                eventId, senderEmail, recipient.getEmail());
    }

    /**
     * Search users by email (excluding current user)
     */
    public List<UserSearchDTO> searchUsers(String query, String currentUserEmail) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        // Search by email (case-insensitive partial match)
        List<Organizer> users = organizerRepository.findByEmailContainingIgnoreCase(query).stream()
                .filter(u -> !u.getEmail().equalsIgnoreCase(currentUserEmail)) // Exclude self
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
     * Get all events user collaborates on (only ACCEPTED)
     */
    public List<Long> getCollaboratedEventIds(String userEmail) {
        Organizer user = organizerRepository.findByEmail(userEmail).orElse(null);
        if (user == null)
            return List.of();

        return collaboratorRepository.findByUserId(user.getId()).stream()
                .filter(c -> "ACCEPTED".equals(c.getStatus()))
                .map(EventCollaborator::getEventId)
                .collect(Collectors.toList());
    }

    /**
     * Log an action done by a user in an event
     */
    @Transactional
    public void logAction(Long eventId, String userEmail, String action, String details) {
        Organizer user = organizerRepository.findByEmail(userEmail).orElse(null);
        if (user == null)
            return;

        EventLog log = new EventLog();
        log.setEventId(eventId);
        log.setUserId(user.getId());
        log.setAction(action);
        log.setDetails(details);
        eventLogRepository.save(log);
    }

    /**
     * Get activity logs for a specific collaborator in an event
     */
    public List<EventLogDTO> getCollaboratorLogs(Long eventId, Long userId) {
        List<EventLog> logs = eventLogRepository.findByEventIdAndUserIdOrderByTimestampDesc(eventId, userId);
        Organizer user = organizerRepository.findById(userId).orElse(null);
        String userName = user != null ? user.getFullName() : "Unknown";

        return logs.stream().map(logEntry -> {
            EventLogDTO dto = new EventLogDTO();
            dto.setId(logEntry.getId());
            dto.setEventId(logEntry.getEventId());
            dto.setUserId(logEntry.getUserId());
            dto.setUserName(userName);
            dto.setAction(logEntry.getAction());
            dto.setDetails(logEntry.getDetails());
            dto.setTimestamp(logEntry.getTimestamp());
            return dto;
        }).collect(Collectors.toList());
    }
}
