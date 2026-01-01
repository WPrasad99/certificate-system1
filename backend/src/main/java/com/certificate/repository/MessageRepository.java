package com.certificate.repository;

import com.certificate.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByEventIdAndReceiverIdOrderByTimestampDesc(Long eventId, Long receiverId);

    List<Message> findByReceiverIdAndIsReadFalse(Long receiverId);

    List<Message> findByEventIdAndReceiverIdAndIsReadFalse(Long eventId, Long receiverId);
}
