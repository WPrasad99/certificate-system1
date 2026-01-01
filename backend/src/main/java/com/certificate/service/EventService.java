package com.certificate.service;

import com.certificate.dto.EventRequest;
import com.certificate.entity.Event;
import com.certificate.entity.Organizer;
import com.certificate.repository.CertificateRepository;
import com.certificate.repository.TemplateRepository;
import com.certificate.repository.EventRepository;
import com.certificate.repository.ParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final ParticipantRepository participantRepository;
    private final CertificateRepository certificateRepository;
    private final TemplateRepository templateRepository;
    private final AuthService authService;
    private final CollaborationService collaborationService;
    private final com.certificate.repository.EventCollaboratorRepository eventCollaboratorRepository;
    private final com.certificate.repository.CollaborationRequestRepository collaborationRequestRepository;

    public Event createEvent(EventRequest request, String email) {
        Organizer organizer = authService.getOrganizerByEmail(email);

        Event event = new Event();
        event.setEventName(request.getEventName());
        event.setEventDate(request.getEventDate());
        event.setOrganizerName(request.getOrganizerName());
        event.setInstituteName(request.getInstituteName());
        event.setOrganizerId(organizer.getId());

        return eventRepository.save(event);
    }

    public List<Event> getAllEvents(String email) {
        Organizer organizer = authService.getOrganizerByEmail(email);
        List<Event> ownedEvents = eventRepository.findByOrganizerId(organizer.getId());

        // Add collaborated events
        List<Long> collaboratedEventIds = collaborationService.getCollaboratedEventIds(email);
        List<Event> collaboratedEvents = collaboratedEventIds.stream()
                .map(eventRepository::findById)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .collect(java.util.stream.Collectors.toList());

        // Combine and deduplicate
        java.util.Set<Long> eventIds = new java.util.HashSet<>();
        java.util.List<Event> allEvents = new java.util.ArrayList<>();

        for (Event event : ownedEvents) {
            if (eventIds.add(event.getId())) {
                allEvents.add(event);
            }
        }
        for (Event event : collaboratedEvents) {
            if (eventIds.add(event.getId())) {
                allEvents.add(event);
            }
        }

        return allEvents;
    }

    public Event getEventById(Long eventId, String email) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        Organizer organizer = authService.getOrganizerByEmail(email);

        // Check if user is owner OR collaborator
        boolean isOwner = event.getOrganizerId().equals(organizer.getId());
        boolean isCollaborator = collaborationService.isCollaborator(eventId, email);

        if (!isOwner && !isCollaborator) {
            throw new RuntimeException("Unauthorized access to event");
        }

        return event;
    }

    public Event updateEvent(Long eventId, EventRequest request, String email) {
        Event event = getEventById(eventId, email);

        event.setEventName(request.getEventName());
        event.setEventDate(request.getEventDate());
        event.setOrganizerName(request.getOrganizerName());
        event.setInstituteName(request.getInstituteName());

        return eventRepository.save(event);
    }

    @Transactional
    public void deleteEvent(Long eventId, String email) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        Organizer organizer = authService.getOrganizerByEmail(email);

        // Only owner can delete (not collaborators)
        if (!event.getOrganizerId().equals(organizer.getId())) {
            throw new RuntimeException("Only event owner can delete the event");
        }

        // Manual cascading deletion
        certificateRepository.deleteByEventId(eventId);
        participantRepository.deleteByEventId(eventId);
        templateRepository.deleteAll(templateRepository.findByEventId(eventId).stream().toList());

        // Delete collaboration data
        eventCollaboratorRepository.findByEventId(eventId).forEach(eventCollaboratorRepository::delete);
        collaborationRequestRepository.findByEventId(eventId).forEach(collaborationRequestRepository::delete);

        eventRepository.delete(event);
    }

    /**
     * Internal method to get event by ID without authentication check
     * Used for public certificate verification
     */
    public Event getEventByIdInternal(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
    }
}
