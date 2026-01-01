package com.certificate.util;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSchemaFixer {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void fixSchema() {
        log.info("Checking for database schema fixes...");
        try {
            // Add status column if missing in event_collaborators
            jdbcTemplate.execute(
                    "ALTER TABLE event_collaborators ADD COLUMN IF NOT EXISTS status VARCHAR(255) DEFAULT 'PENDING'");

            // Add added_at column if missing in event_collaborators
            jdbcTemplate.execute("ALTER TABLE event_collaborators ADD COLUMN IF NOT EXISTS added_at TIMESTAMP");
            // Set default for added_at if it was just added
            jdbcTemplate.execute("UPDATE event_collaborators SET added_at = CURRENT_TIMESTAMP WHERE added_at IS NULL");

            // Fix collaboration_requests table as well
            jdbcTemplate.execute(
                    "ALTER TABLE collaboration_requests ADD COLUMN IF NOT EXISTS status VARCHAR(255) DEFAULT 'PENDING'");
            jdbcTemplate.execute("ALTER TABLE collaboration_requests ADD COLUMN IF NOT EXISTS created_at TIMESTAMP");
            jdbcTemplate.execute(
                    "UPDATE collaboration_requests SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL");

            // Create event_logs table if not exists
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS event_logs (" +
                    "id SERIAL PRIMARY KEY, " +
                    "event_id BIGINT NOT NULL, " +
                    "user_id BIGINT NOT NULL, " +
                    "action VARCHAR(255) NOT NULL, " +
                    "details VARCHAR(1000), " +
                    "timestamp TIMESTAMP NOT NULL)");

            // Create messages table if not exists
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS messages (" +
                    "id SERIAL PRIMARY KEY, " +
                    "event_id BIGINT NOT NULL, " +
                    "sender_id BIGINT NOT NULL, " +
                    "receiver_id BIGINT NOT NULL, " +
                    "content TEXT NOT NULL, " +
                    "timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "is_read BOOLEAN NOT NULL DEFAULT FALSE)");

            log.info("Database schema fixes applied successfully.");
        } catch (Exception e) {
            log.warn("Database schema fix failed (it might already be correct): {}", e.getMessage());
        }
    }
}
