package com.certificate.repository;

import com.certificate.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    List<Certificate> findByEventId(Long eventId);

    Optional<Certificate> findByParticipantId(Long participantId);

    Optional<Certificate> findByVerificationId(String verificationId);

    void deleteByEventId(Long eventId);

    long countByEventIdIn(List<Long> eventIds);

    List<Certificate> findByEventIdIn(List<Long> eventIds);
}
