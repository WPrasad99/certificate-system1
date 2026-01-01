package com.certificate.repository;

import com.certificate.entity.CollaborationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollaborationRequestRepository extends JpaRepository<CollaborationRequest, Long> {

    List<CollaborationRequest> findByRecipientEmailAndStatus(String recipientEmail, String status);

    List<CollaborationRequest> findByEventIdAndSenderId(Long eventId, Long senderId);

    Optional<CollaborationRequest> findByEventIdAndRecipientEmailAndStatus(Long eventId, String recipientEmail,
            String status);

    boolean existsByEventIdAndRecipientEmailAndStatus(Long eventId, String recipientEmail, String status);

    List<CollaborationRequest> findByEventId(Long eventId);

    List<CollaborationRequest> findBySenderIdAndStatusNot(Long senderId, String status);
}
