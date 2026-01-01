package com.certificate.service;

import com.certificate.dto.MessageDTO;
import com.certificate.dto.MessageRequest;
import com.certificate.entity.Message;
import com.certificate.entity.Organizer;
import com.certificate.repository.MessageRepository;
import com.certificate.repository.OrganizerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final OrganizerRepository organizerRepository;
    private final AuthService authService;

    @Transactional
    public void sendMessages(MessageRequest request, String senderEmail) {
        Organizer sender = authService.getOrganizerByEmail(senderEmail);

        for (Long receiverId : request.getReceiverIds()) {
            Message message = new Message();
            message.setEventId(request.getEventId());
            message.setSenderId(sender.getId());
            message.setReceiverId(receiverId);
            message.setContent(request.getContent());
            messageRepository.save(message);
        }
    }

    public List<MessageDTO> getMessagesForEvent(Long eventId, String receiverEmail) {
        Organizer receiver = authService.getOrganizerByEmail(receiverEmail);
        List<Message> messages = messageRepository.findByEventIdAndReceiverIdOrderByTimestampDesc(eventId,
                receiver.getId());

        return messages.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public long getUnreadCount(String receiverEmail) {
        Organizer receiver = authService.getOrganizerByEmail(receiverEmail);
        return messageRepository.findByReceiverIdAndIsReadFalse(receiver.getId()).size();
    }

    public List<MessageDTO> getUnreadMessages(String receiverEmail) {
        Organizer receiver = authService.getOrganizerByEmail(receiverEmail);
        List<Message> unread = messageRepository.findByReceiverIdAndIsReadFalse(receiver.getId());
        return unread.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(Long eventId, String receiverEmail) {
        Organizer receiver = authService.getOrganizerByEmail(receiverEmail);
        List<Message> unread = messageRepository.findByEventIdAndReceiverIdAndIsReadFalse(eventId, receiver.getId());
        unread.forEach(m -> m.setRead(true));
        messageRepository.saveAll(unread);
    }

    private MessageDTO convertToDTO(Message message) {
        Organizer sender = organizerRepository.findById(message.getSenderId()).orElse(null);
        String senderName = sender != null ? sender.getFullName() : "Unknown";

        return new MessageDTO(
                message.getId(),
                message.getEventId(),
                message.getSenderId(),
                senderName,
                message.getContent(),
                message.getTimestamp(),
                message.isRead());
    }
}
