package com.certificate.controller;

import com.certificate.dto.MessageDTO;
import com.certificate.dto.MessageRequest;
import com.certificate.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/collaboration/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/send")
    public ResponseEntity<?> sendMessages(@RequestBody MessageRequest request, Authentication authentication) {
        try {
            messageService.sendMessages(request, authentication.getName());
            return ResponseEntity.ok(Map.of("message", "Message sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<MessageDTO>> getMessagesForEvent(@PathVariable Long eventId,
            Authentication authentication) {
        return ResponseEntity.ok(messageService.getMessagesForEvent(eventId, authentication.getName()));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        return ResponseEntity.ok(Map.of("count", messageService.getUnreadCount(authentication.getName())));
    }

    @GetMapping("/unread")
    public ResponseEntity<List<MessageDTO>> getUnreadMessages(Authentication authentication) {
        return ResponseEntity.ok(messageService.getUnreadMessages(authentication.getName()));
    }

    @PostMapping("/event/{eventId}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long eventId, Authentication authentication) {
        try {
            messageService.markAsRead(eventId, authentication.getName());
            return ResponseEntity.ok(Map.of("message", "Messages marked as read"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
