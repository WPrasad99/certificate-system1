package com.certificate.service;

import com.certificate.entity.Certificate;
import com.certificate.entity.Event;
import com.certificate.entity.Participant;
import com.certificate.repository.CertificateRepository;
import com.certificate.repository.ParticipantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailDispatchService {

    private final JavaMailSender mailSender;
    private final CertificateRepository certificateRepository;
    private final ParticipantRepository participantRepository;
    private final EventService eventService;

    @org.springframework.beans.factory.annotation.Value("${spring.mail.username}")
    private String mailFrom;

    /**
     * Sends a batch of emails asynchronously by triggering individual tasks.
     * This maximizes parallelism across the entire thread pool.
     */
    public void sendEmailsBatchAsync(java.util.List<Long> certificateIds, Long eventId) {
        log.info("Triggering independent async sends for {} certificates in event {}", certificateIds.size(), eventId);

        // Fetch event once to pass to avoid redundant DB hits in threads
        Event event = eventId != null ? eventService.getEventByIdInternal(eventId) : null;

        for (Long certId : certificateIds) {
            // Trigger each as an independent async task
            sendEmailAsyncInternal(certId, event);
        }
    }

    @Async("taskExecutor")
    public void sendEmailAsyncInternal(Long certificateId, Event event) {
        sendEmailInternal(certificateId, event);
    }

    @Async("taskExecutor")
    public void sendEmailAsync(Long certificateId, String senderEmail) {
        sendEmailInternal(certificateId, null);
    }

    private void sendEmailInternal(Long certificateId, Event preloadedEvent) {
        log.info("Processing email for certificate: {}", certificateId);
        Certificate certificate = certificateRepository.findById(certificateId).orElse(null);
        if (certificate == null || !"GENERATED".equals(certificate.getGenerationStatus()))
            return;

        try {
            certificate.setEmailStatus("SENDING");
            certificateRepository.saveAndFlush(certificate);

            Participant participant = participantRepository.findById(certificate.getParticipantId())
                    .orElseThrow(() -> new RuntimeException("Participant not found"));

            Event event = preloadedEvent != null ? preloadedEvent
                    : eventService.getEventByIdInternal(certificate.getEventId());

            File file = new File(certificate.getFilePath());
            if (!file.exists())
                return;
            byte[] certData = Files.readAllBytes(file.toPath());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(participant.getEmail());
            helper.setSubject("Certificate: Bhartiayam '25 - " + event.getEventName());

            String content = "We're pleased to share your Certificate of Participation for <b>Bharatiyam '25</b> organized by <b>BVDUCOEP</b>. "
                    +
                    "Thank you for your enthusiasm and active participation throughout the event.<br/><br/>" +
                    "Please find your certificate attached to this email. You may download and print it for your records. "
                    +
                    "Once again, thank you for being a part of <b>Bharatiyam '25</b>. We wish you all the very best.";

            String htmlBody = generateBrandedHtml(participant.getName(), "Bhartiayam '25 Certificate", content);

            helper.setText(htmlBody, true);
            helper.addAttachment(file.getName(), new ByteArrayResource(certData));

            attachBanner(helper);

            mailSender.send(message);

            certificate.setEmailStatus("SENT");
            certificate.setEmailSentAt(LocalDateTime.now());
            certificateRepository.saveAndFlush(certificate);
        } catch (Exception e) {
            log.error("Failed to send email for cert: " + certificateId, e);
            certificateRepository.findById(certificateId).ifPresent(cert -> {
                cert.setEmailStatus("FAILED");
                cert.setErrorMessage(e.getMessage());
                certificateRepository.saveAndFlush(cert);
            });
        }
    }

    @Async("taskExecutor")
    public void sendUpdateEmailsBatchAsync(java.util.List<Long> participantIds, String subject, String content,
            String organizerName) {
        log.info("Starting batch update email send for {} participants", participantIds.size());
        for (Long id : participantIds) {
            try {
                sendUpdateEmailAsync(id, subject, content, organizerName);
            } catch (Exception e) {
                log.error("Error triggering update email for participant {}", id, e);
            }
        }
    }

    @Async("taskExecutor")
    public void sendUpdateEmailAsync(Long participantId, String subject, String content, String organizerName) {
        Participant participant = participantRepository.findById(participantId).orElse(null);
        if (participant == null)
            return;

        try {
            participant.setUpdateEmailStatus("SENDING");
            participantRepository.saveAndFlush(participant);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(mailFrom);
            helper.setTo(participant.getEmail());
            helper.setSubject(subject);

            String htmlBody = generateBrandedHtml(participant.getName(), subject, content);
            helper.setText(htmlBody, true);

            attachBanner(helper);

            mailSender.send(message);

            participant.setUpdateEmailStatus("SENT");
            participantRepository.saveAndFlush(participant);
            log.info("Update email sent successfully to: {}", participant.getEmail());
        } catch (Exception e) {
            log.error("Failed to send update email to: " + participant.getEmail(), e);
            participant.setUpdateEmailStatus("FAILED");
            participantRepository.saveAndFlush(participant);
        }
    }

    private String generateBrandedHtml(String name, String title, String body) {
        return "<html><body style='text-align: center; font-family: Arial, sans-serif;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #eee; border-radius: 12px;'>"
                +
                "<img src='cid:banner' style='width: 100%; max-width: 600px; margin-bottom: 25px; border-radius: 8px;' alt='Banner'><br/>"
                +
                "<h2 style='color: #1e3a8a;'>Dear " + name + ",</h2>" +
                "<div style='font-size: 16px; line-height: 1.8; color: #444; margin: 20px 0;'>" +
                body +
                "</div>" +
                "<p style='font-size: 16px; font-weight: bold; color: #1e3a8a; margin-top: 30px;'>" +
                "Warm regards,<br/>BVDUCOEP" +
                "</p>" +
                "</div></body></html>";
    }

    private void attachBanner(MimeMessageHelper helper) throws Exception {
        // Try multiple locations for the banner to ensure visibility
        org.springframework.core.io.Resource banner = new org.springframework.core.io.ClassPathResource(
                "static/images/email_banner.jpg");
        if (!banner.exists()) {
            // Fallback to absolute path if classpath fails in some environments
            File fallback = new File("backend/src/main/resources/static/images/email_banner.jpg");
            if (fallback.exists()) {
                helper.addInline("banner", new org.springframework.core.io.FileSystemResource(fallback));
                return;
            }
        }

        if (banner.exists()) {
            helper.addInline("banner", banner);
        } else {
            log.warn("Email banner NOT found in resources or fallback path!");
        }
    }
}
