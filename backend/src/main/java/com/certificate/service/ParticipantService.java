package com.certificate.service;

import com.certificate.dto.ParticipantDTO;
import com.certificate.entity.Event;
import com.certificate.entity.Participant;
import com.certificate.repository.ParticipantRepository;
import com.certificate.util.FileParserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final EventService eventService;
    private final FileParserUtil fileParserUtil;
    private final CollaborationService collaborationService;

    @Transactional
    public List<ParticipantDTO> uploadParticipants(Long eventId, MultipartFile file, String email) throws Exception {
        // Verify event access
        Event event = eventService.getEventById(eventId, email);

        // Parse file
        List<ParticipantDTO> participantDTOs = fileParserUtil.parseFile(file);

        // Save new participants (only if they don't already exist)
        List<Participant> participantsToSave = new java.util.ArrayList<>();

        for (ParticipantDTO dto : participantDTOs) {
            // Check if participant with same email already exists for this event
            if (participantRepository.findByEventIdAndEmail(eventId, dto.getEmail()).isEmpty()) {
                Participant p = new Participant();
                p.setName(dto.getName());
                p.setEmail(dto.getEmail());
                p.setEventId(eventId);
                participantsToSave.add(p);
            }
        }

        if (!participantsToSave.isEmpty()) {
            participantRepository.saveAll(participantsToSave);
            collaborationService.logAction(eventId, email, "UPLOAD_PARTICIPANTS",
                    "Uploaded " + participantsToSave.size() + " participants");
        }

        // Return all participants for this event
        return participantRepository.findByEventId(eventId).stream()
                .map(p -> new ParticipantDTO(p.getId(), p.getName(), p.getEmail(), p.getEventId()))
                .collect(Collectors.toList());
    }

    public List<ParticipantDTO> getParticipantsByEvent(Long eventId, String email) {
        // Verify event access
        eventService.getEventById(eventId, email);

        return participantRepository.findByEventId(eventId).stream()
                .map(p -> new ParticipantDTO(p.getId(), p.getName(), p.getEmail(), p.getEventId()))
                .collect(Collectors.toList());
    }

    public void deleteParticipant(Long participantId, String email) {
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new RuntimeException("Participant not found"));

        // Verify event access
        eventService.getEventById(participant.getEventId(), email);

        participantRepository.delete(participant);
        collaborationService.logAction(participant.getEventId(), email, "REMOVE_PARTICIPANT",
                "Removed participant: " + participant.getName());
    }

    @Transactional
    public void deleteAllParticipants(Long eventId, String email) {
        // Verify event access
        eventService.getEventById(eventId, email);

        participantRepository.deleteByEventId(eventId);
        collaborationService.logAction(eventId, email, "CLEAR_PARTICIPANTS", "Removed all participants");
    }
}
