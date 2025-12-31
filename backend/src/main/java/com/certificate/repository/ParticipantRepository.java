package com.certificate.repository;

import com.certificate.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    List<Participant> findByEventId(Long eventId);

    java.util.Optional<Participant> findByEventIdAndEmail(Long eventId, String email);

    void deleteByEventId(Long eventId);
}
