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
        return eventRepository.findByOrganizerId(organizer.getId());
    }

    public Event getEventById(Long eventId, String email) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        Organizer organizer = authService.getOrganizerByEmail(email);
        if (!event.getOrganizerId().equals(organizer.getId())) {
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
        Event event = getEventById(eventId, email);

        // Manual cascading deletion
        certificateRepository.deleteByEventId(eventId);
        participantRepository.deleteByEventId(eventId);
        templateRepository.deleteAll(templateRepository.findByEventId(eventId).stream().toList());

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
