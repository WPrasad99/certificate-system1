package com.certificate.repository;

import com.certificate.entity.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {
    Optional<Template> findByEventId(Long eventId);

    Optional<Template> findByIsDefaultTrue();
}
