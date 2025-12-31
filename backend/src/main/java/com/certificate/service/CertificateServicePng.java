package com.certificate.service;

import com.certificate.dto.CertificateStatusDTO;
import com.certificate.entity.Certificate;
import com.certificate.entity.Event;
import com.certificate.entity.Participant;
import com.certificate.entity.Template;
import com.certificate.repository.CertificateRepository;
import com.certificate.repository.ParticipantRepository;
import com.certificate.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateServicePng {

    private final CertificateRepository certificateRepository;
    private final ParticipantRepository participantRepository;
    private final TemplateRepository templateRepository;
    private final EventService eventService;
    private final PngCertificateGenerator pngGenerator;
    private final EmailDispatchService emailDispatchService;
    private final com.certificate.util.QRCodeGenerator qrCodeGenerator;
    // Removed JavaMailSender injection from here as it's moved to
    // EmailDispatchService

    private static final String STORAGE_PATH = "./certificates/";

    @Transactional
    public void generateCertificates(Long eventId, String email) {
        Event event = eventService.getEventById(eventId, email);
        List<Participant> participants = participantRepository.findByEventId(eventId);

        if (participants.isEmpty()) {
            throw new RuntimeException("No participants found for this event");
        }

        // Get template (custom or default)
        byte[] templateImage = templateRepository.findByEventId(eventId)
                .map(Template::getImageData)
                .orElse(null);

        // Get the frontend URL for QR code verification
        String baseUrl = getVerificationUrl();

        // Find existing certificates for this event to check for already generated ones
        List<Certificate> existingCertificates = certificateRepository.findByEventId(eventId);

        // Generate certificate for each participant
        for (Participant participant : participants) {
            // Check if participant already has a GENERATED certificate
            boolean alreadyGenerated = existingCertificates.stream()
                    .anyMatch(c -> c.getParticipantId().equals(participant.getId())
                            && "GENERATED".equals(c.getGenerationStatus()));

            if (alreadyGenerated) {
                log.info("Certificate already generated for participant: {}. Skipping.", participant.getName());
                continue;
            }

            // Remove any pending/failed certificate for this participant before creating a
            // new one
            existingCertificates.stream()
                    .filter(c -> c.getParticipantId().equals(participant.getId()))
                    .forEach(certificateRepository::delete);

            Certificate certificate = new Certificate();
            certificate.setParticipantId(participant.getId());
            certificate.setEventId(eventId);
            certificate.setGenerationStatus("PENDING");

            // Generate unique verification ID
            String verificationId = java.util.UUID.randomUUID().toString();
            certificate.setVerificationId(verificationId);

            try {
                // Generate QR code with verification URL
                String baseUrl = getVerificationUrl();
                String verificationUrl = baseUrl + "/verify/" + verificationId;
                java.awt.image.BufferedImage qrCode = qrCodeGenerator.generateQRCode(verificationUrl, 200, 200);

                // Generate certificate PNG with QR code
                byte[] certData = pngGenerator.generateCertificatePdf(templateImage, participant.getName(), qrCode);

                // Save to file system (organized by event folder)
                String fileName = generateFileName(participant.getName(), participant.getId());
                String filePath = saveCertificateToFile(certData, event.getEventName(), fileName);

                certificate.setFilePath(filePath);
                certificate.setGenerationStatus("GENERATED");
                certificate.setGeneratedAt(LocalDateTime.now());

                log.info("Generated certificate with verification ID: {} for: {}", verificationId,
                        participant.getName());
            } catch (Throwable e) {
                log.error("Failed to generate certificate for participant: " + participant.getId(), e);
                certificate.setGenerationStatus("FAILED");
                certificate.setErrorMessage(e.getMessage());
            }

            certificateRepository.save(certificate);
        }
    }

    @org.springframework.beans.factory.annotation.Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * Get the verification URL for QR codes (configurable for production)
     */
    private String getVerificationUrl() {
        // Use configured frontend URL from environment variable
        // In production, set FRONTEND_URL environment variable to your deployed
        // frontend
        log.info("Using frontend URL for QR codes: {}", frontendUrl);
        return frontendUrl;
    }

    public List<CertificateStatusDTO> getCertificateStatus(Long eventId, String email) {
        eventService.getEventById(eventId, email);

        List<Certificate> certificates = certificateRepository.findByEventId(eventId);
        List<Participant> participants = participantRepository.findByEventId(eventId);

        return participants.stream()
                .map(participant -> {
                    Certificate cert = certificates.stream()
                            .filter(c -> c.getParticipantId().equals(participant.getId()))
                            .findFirst()
                            .orElse(null);

                    CertificateStatusDTO dto = new CertificateStatusDTO();
                    dto.setParticipantName(participant.getName());
                    dto.setEmail(participant.getEmail());

                    if (cert != null) {
                        dto.setId(cert.getId());
                        dto.setGenerationStatus(cert.getGenerationStatus());
                        dto.setEmailStatus(cert.getEmailStatus());
                        dto.setUpdateEmailStatus(participant.getUpdateEmailStatus());
                    } else {
                        dto.setGenerationStatus("NOT_GENERATED");
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    public Resource downloadCertificate(Long certificateId, String email) {
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));

        Event event = eventService.getEventById(certificate.getEventId(), email);

        try {
            File file = new File(certificate.getFilePath());
            byte[] data = Files.readAllBytes(file.toPath());
            return new ByteArrayResource(data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to download certificate", e);
        }
    }

    public Resource downloadAllCertificates(Long eventId, String email) {
        Event event = eventService.getEventById(eventId, email);
        List<Certificate> certificates = certificateRepository.findByEventId(eventId);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (Certificate cert : certificates) {
                if ("GENERATED".equals(cert.getGenerationStatus()) && cert.getFilePath() != null) {
                    File file = new File(cert.getFilePath());
                    if (file.exists()) {
                        byte[] data = Files.readAllBytes(file.toPath());
                        ZipEntry entry = new ZipEntry(file.getName());
                        zos.putNextEntry(entry);
                        zos.write(data);
                        zos.closeEntry();
                    } else {
                        log.warn("Certificate file not found: {}", cert.getFilePath());
                    }
                }
            }

            zos.finish();
            return new ByteArrayResource(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create ZIP file", e);
        }
    }

    public void sendCertificateByEmail(Long certificateId, String senderEmail) {
        // Use async send for fast UI response
        emailDispatchService.sendEmailAsync(certificateId, senderEmail);
    }

    public void sendAllCertificates(Long eventId, String senderEmail) {
        // Check ownership once
        eventService.getEventById(eventId, senderEmail);

        List<Certificate> certificates = certificateRepository.findByEventId(eventId).stream()
                .filter(c -> "GENERATED".equals(c.getGenerationStatus()))
                .collect(Collectors.toList());

        if (certificates.isEmpty())
            return;

        // Chunk into batches of 50 for maximum throughput per thread
        int batchSize = 50;
        for (int i = 0; i < certificates.size(); i += batchSize) {
            List<Long> batchIds = certificates.subList(i, Math.min(i + batchSize, certificates.size()))
                    .stream()
                    .map(Certificate::getId)
                    .collect(Collectors.toList());
            emailDispatchService.sendEmailsBatchAsync(batchIds, eventId);
        }
    }

    public void sendUpdateEmails(Long eventId, String subject, String content, String senderEmail) {
        Event event = eventService.getEventById(eventId, senderEmail);
        List<Long> participantIds = participantRepository.findByEventId(eventId).stream()
                .map(Participant::getId)
                .collect(Collectors.toList());

        if (participantIds.isEmpty())
            return;

        // Chunk updates for massive parallelism
        int batchSize = 50;
        for (int i = 0; i < participantIds.size(); i += batchSize) {
            List<Long> batchIds = participantIds.subList(i, Math.min(i + batchSize, participantIds.size()));
            emailDispatchService.sendUpdateEmailsBatchAsync(batchIds, subject, content, event.getOrganizerName());
        }
    }

    private String generateFileName(String participantName, Long participantId) {
        String sanitizedName = participantName.replaceAll("[^a-zA-Z0-9]", "_");
        return sanitizedName + "_" + participantId + ".pdf";
    }

    private String saveCertificateToFile(byte[] data, String eventName, String fileName) throws Exception {
        String sanitizedEventName = eventName.replaceAll("[^a-zA-Z0-9 ]", "_");
        File eventDir = new File(STORAGE_PATH + sanitizedEventName);

        if (!eventDir.exists()) {
            eventDir.mkdirs();
        }

        String filePath = eventDir.getPath() + File.separator + fileName;
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(data);
        }

        log.info("Saved certificate for event '{}' to: {}", eventName, filePath);
        return filePath;
    }

    /**
     * Verify certificate by verification ID (public endpoint, no auth required)
     */
    public com.certificate.dto.CertificateVerificationDTO verifyCertificate(String verificationId) {
        Certificate certificate = certificateRepository.findByVerificationId(verificationId)
                .orElseThrow(() -> new RuntimeException("Certificate not found or invalid"));

        if (!"GENERATED".equals(certificate.getGenerationStatus())) {
            throw new RuntimeException("Certificate is not valid");
        }

        // Get participant details
        Participant participant = participantRepository.findById(certificate.getParticipantId())
                .orElseThrow(() -> new RuntimeException("Participant not found"));

        // Get event details
        Event event = eventService.getEventByIdInternal(certificate.getEventId());

        // Build verification response
        com.certificate.dto.CertificateVerificationDTO dto = new com.certificate.dto.CertificateVerificationDTO();
        dto.setValid(true);
        dto.setParticipantName(participant.getName());
        dto.setEventName(event.getEventName());
        dto.setEventDate(event.getEventDate());
        dto.setOrganizerName(event.getOrganizerName());
        dto.setInstituteName(event.getInstituteName());
        dto.setGeneratedAt(certificate.getGeneratedAt());
        dto.setVerificationId(verificationId);

        return dto;
    }
}
