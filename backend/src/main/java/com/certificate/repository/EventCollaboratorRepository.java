package com.certificate.repository;

import com.certificate.entity.EventCollaborator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventCollaboratorRepository extends JpaRepository<EventCollaborator, Long> {

    List<EventCollaborator> findByEventId(Long eventId);

    List<EventCollaborator> findByUserId(Long userId);

    Optional<EventCollaborator> findByEventIdAndUserId(Long eventId, Long userId);

    boolean existsByEventIdAndUserId(Long eventId, Long userId);

    void deleteByEventIdAndUserId(Long eventId, Long userId);

    @Query("SELECT ec.eventId FROM EventCollaborator ec WHERE ec.userId = :userId")
    List<Long> findEventIdsByUserId(Long userId);
}
