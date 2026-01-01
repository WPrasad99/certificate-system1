package com.certificate.repository;

import com.certificate.entity.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventLogRepository extends JpaRepository<EventLog, Long> {
    List<EventLog> findByEventIdOrderByTimestampDesc(Long eventId);

    List<EventLog> findByEventIdAndUserIdOrderByTimestampDesc(Long eventId, Long userId);

    List<EventLog> findByEventIdInOrderByTimestampDesc(List<Long> eventIds);
}
