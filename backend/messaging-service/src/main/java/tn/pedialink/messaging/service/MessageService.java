package tn.pedialink.messaging.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tn.pedialink.messaging.dto.MessageResponse;
import tn.pedialink.messaging.dto.SendMessageRequest;
import tn.pedialink.messaging.model.Message;
import tn.pedialink.messaging.repository.MessageRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationService conversationService;

    public MessageResponse sendMessage(SendMessageRequest request) {
        log.info("Sending message in conversation: {}", request.getConversationId());

        Message message = Message.builder()
                .conversationId(request.getConversationId())
                .senderId(request.getSenderId())
                .senderType(request.getSenderType())
                .content(request.getContent())
                .attachments(request.getAttachments() != null ? request.getAttachments() : new ArrayList<>())
                .isRead(false)
                .priority(request.getPriority() != null ? request.getPriority() : Message.Priority.NORMAL)
                .replyToId(request.getReplyToId())
                .isDeleted(false)
                .build();

        Message saved = messageRepository.save(message);
        log.info("Message sent with ID: {}", saved.getId());

        conversationService.updateLastMessage(request.getConversationId(), request.getContent(), request.getSenderId());
        boolean isDoctor = request.getSenderType() == Message.SenderType.PARENT;
        conversationService.incrementUnreadCount(request.getConversationId(), isDoctor);

        return convertToResponse(saved);
    }

    public List<MessageResponse> getConversationMessages(String conversationId) {
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public Page<MessageResponse> getConversationMessagesPaginated(String conversationId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable)
                .map(this::convertToResponse);
    }

    public void markMessagesAsRead(String conversationId, String userId) {
        List<Message> unreadMessages = messageRepository.findByConversationIdAndIsReadFalse(conversationId);
        unreadMessages.forEach(message -> {
            if (!message.getSenderId().equals(userId)) {
                message.setIsRead(true);
                message.setReadAt(LocalDateTime.now());
            }
        });
        messageRepository.saveAll(unreadMessages);
    }

    public Long getUnreadCount(String conversationId) {
        return messageRepository.countByConversationIdAndIsReadFalse(conversationId);
    }

    // Search messages
    public List<MessageResponse> searchMessages(String conversationId, String query) {
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId)
                .stream()
                .filter(m -> !Boolean.TRUE.equals(m.getIsDeleted()))
                .filter(m -> m.getContent() != null && m.getContent().toLowerCase().contains(query.toLowerCase()))
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Add reaction
    public MessageResponse addReaction(String messageId, String emoji, String userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (message.getReactions() == null) message.setReactions(new HashMap<>());
        message.getReactions().computeIfAbsent(emoji, k -> new ArrayList<>());

        List<String> users = message.getReactions().get(emoji);
        if (users.contains(userId)) {
            users.remove(userId); // toggle off
            if (users.isEmpty()) message.getReactions().remove(emoji);
        } else {
            users.add(userId); // toggle on
        }

        return convertToResponse(messageRepository.save(message));
    }

    // Edit message
    public MessageResponse editMessage(String messageId, String senderId, String newContent) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!message.getSenderId().equals(senderId)) {
            throw new RuntimeException("Not authorized to edit this message");
        }

        if (message.getEditHistory() == null) message.setEditHistory(new ArrayList<>());
        message.getEditHistory().add(message.getContent()); // save old content
        message.setContent(newContent);
        message.setEditedAt(LocalDateTime.now());

        return convertToResponse(messageRepository.save(message));
    }

    // Delete message (soft delete)
    public MessageResponse deleteMessage(String messageId, String senderId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!message.getSenderId().equals(senderId)) {
            throw new RuntimeException("Not authorized to delete this message");
        }

        message.setIsDeleted(true);
        message.setDeletedAt(LocalDateTime.now());
        message.setContent("[Message deleted]");

        return convertToResponse(messageRepository.save(message));
    }

    private MessageResponse convertToResponse(Message message) {
        MessageResponse.MessageResponseBuilder builder = MessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversationId())
                .senderId(message.getSenderId())
                .senderType(message.getSenderType())
                .content(message.getContent())
                .attachments(message.getAttachments())
                .isRead(message.getIsRead())
                .readAt(message.getReadAt())
                .createdAt(message.getCreatedAt())
                .priority(message.getPriority())
                .replyToId(message.getReplyToId())
                .reactions(message.getReactions())
                .isDeleted(message.getIsDeleted())
                .editedAt(message.getEditedAt())
                .editHistory(message.getEditHistory());

        // Fetch reply-to message if exists
        if (message.getReplyToId() != null) {
            messageRepository.findById(message.getReplyToId()).ifPresent(replyMsg -> {
                builder.replyTo(MessageResponse.builder()
                        .id(replyMsg.getId())
                        .senderId(replyMsg.getSenderId())
                        .senderType(replyMsg.getSenderType())
                        .content(replyMsg.getContent())
                        .build());
            });
        }

        return builder.build();
    }
}
