package com.certificate.repository;

import com.certificate.entity.Organizer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizerRepository extends JpaRepository<Organizer, Long> {
    Optional<Organizer> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Organizer> findByEmailContainingIgnoreCase(String email);
}
