package com.certificate.repository;

import com.certificate.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByEventIdAndReceiverIdOrderByTimestampDesc(Long eventId, Long receiverId);

    @org.springframework.data.jpa.repository.Query("SELECT m FROM Message m WHERE m.eventId = :eventId AND (m.senderId = :userId OR m.receiverId = :userId) ORDER BY m.timestamp ASC")
    List<Message> findFullMessageHistory(@org.springframework.data.repository.query.Param("eventId") Long eventId,
            @org.springframework.data.repository.query.Param("userId") Long userId);

    List<Message> findByEventIdOrderByTimestampAsc(Long eventId);

    List<Message> findByReceiverIdAndIsReadFalse(Long receiverId);

    List<Message> findByEventIdAndReceiverIdAndIsReadFalse(Long eventId, Long receiverId);
}
