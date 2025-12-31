package com.certificate.controller;

import com.certificate.dto.CertificateStatusDTO;
import com.certificate.dto.EventUpdateEmailRequest;
import com.certificate.service.CertificateServicePng;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateServicePng certificateService;

    @PostMapping("/events/{eventId}/generate")
    public ResponseEntity<?> generateCertificates(@PathVariable Long eventId, Authentication authentication) {
        try {
            certificateService.generateCertificates(eventId, authentication.getName());
            return ResponseEntity.ok(createSuccessResponse("Certificates generated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/events/{eventId}/status")
    public ResponseEntity<?> getCertificateStatus(@PathVariable Long eventId, Authentication authentication) {
        try {
            List<CertificateStatusDTO> status = certificateService.getCertificateStatus(eventId,
                    authentication.getName());
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{certificateId}/download")
    public ResponseEntity<?> downloadCertificate(@PathVariable Long certificateId, Authentication authentication) {
        try {
            Resource resource = certificateService.downloadCertificate(certificateId, authentication.getName());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"certificate.pdf\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/events/{eventId}/download-all")
    public ResponseEntity<?> downloadAllCertificates(@PathVariable Long eventId, Authentication authentication) {
        try {
            Resource resource = certificateService.downloadAllCertificates(eventId, authentication.getName());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"certificates.zip\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/{certificateId}/send-email")
    public ResponseEntity<?> sendCertificateByEmail(@PathVariable Long certificateId, Authentication authentication) {
        try {
            certificateService.sendCertificateByEmail(certificateId, authentication.getName());
            return ResponseEntity.ok(createSuccessResponse("Certificate emailed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/events/{eventId}/send-all")
    public ResponseEntity<?> sendAllCertificates(@PathVariable Long eventId, Authentication authentication) {
        try {
            certificateService.sendAllCertificates(eventId, authentication.getName());
            return ResponseEntity.ok(createSuccessResponse("Certificates sent to all participants"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/events/{eventId}/send-updates")
    public ResponseEntity<?> sendUpdateEmails(
            @PathVariable Long eventId,
            @RequestBody EventUpdateEmailRequest request,
            Authentication authentication) {
        log.info("Received request to send update emails for event: {} by user: {}", eventId, authentication.getName());
        try {
            certificateService.sendUpdateEmails(
                    eventId,
                    request.getSubject(),
                    request.getContent(),
                    authentication.getName());
            return ResponseEntity.ok(createSuccessResponse("Update emails are being sent to all participants"));
        } catch (Exception e) {
            log.error("Failed to send update emails for event: " + eventId, e);
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return response;
    }

    private Map<String, String> createSuccessResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }

    @GetMapping("/verify/{verificationId}")
    public ResponseEntity<?> verifyCertificate(@PathVariable String verificationId) {
        try {
            com.certificate.dto.CertificateVerificationDTO verification = certificateService
                    .verifyCertificate(verificationId);
            return ResponseEntity.ok(verification);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
}
