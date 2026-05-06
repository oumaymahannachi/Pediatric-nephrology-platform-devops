package tn.pedialink.messaging.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.pedialink.messaging.dto.ConversationResponse;
import tn.pedialink.messaging.dto.CreateConversationRequest;
import tn.pedialink.messaging.model.Conversation;
import tn.pedialink.messaging.repository.ConversationRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationService {

    private final ConversationRepository conversationRepository;

    public ConversationResponse createConversation(CreateConversationRequest request) {
        log.info("Creating conversation between doctor {} and parent {}", 
                request.getDoctorId(), request.getParentId());

        // Check if conversation already exists
        Optional<Conversation> existing = conversationRepository
                .findByDoctorIdAndParentIdAndChildId(
                        request.getDoctorId(),
                        request.getParentId(),
                        request.getChildId()
                );

        if (existing.isPresent()) {
            log.info("Conversation already exists: {}", existing.get().getId());
            return convertToResponse(existing.get());
        }

        Conversation conversation = Conversation.builder()
                .doctorId(request.getDoctorId())
                .parentId(request.getParentId())
                .childId(request.getChildId())
                .subject(request.getSubject())
                .contextType(request.getContextType())
                .contextId(request.getContextId())
                .doctorUnreadCount(0)
                .parentUnreadCount(0)
                .status(Conversation.ConversationStatus.ACTIVE)
                .build();

        Conversation saved = conversationRepository.save(conversation);
        log.info("Conversation created with ID: {}", saved.getId());

        return convertToResponse(saved);
    }

    public List<ConversationResponse> getDoctorConversations(String doctorId) {
        log.info("Getting conversations for doctor: {}", doctorId);
        return conversationRepository.findByDoctorIdOrderByLastMessageTimeDesc(doctorId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<ConversationResponse> getParentConversations(String parentId) {
        log.info("Getting conversations for parent: {}", parentId);
        return conversationRepository.findByParentIdOrderByLastMessageTimeDesc(parentId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public ConversationResponse getConversationById(String id) {
        log.info("Getting conversation: {}", id);
        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conversation not found: " + id));
        return convertToResponse(conversation);
    }

    public void updateLastMessage(String conversationId, String content, String senderId) {
        conversationRepository.findById(conversationId).ifPresent(conversation -> {
            conversation.setLastMessageContent(content);
            conversation.setLastMessageTime(java.time.LocalDateTime.now());
            conversation.setLastMessageSenderId(senderId);
            conversationRepository.save(conversation);
        });
    }

    public void incrementUnreadCount(String conversationId, boolean isDoctor) {
        conversationRepository.findById(conversationId).ifPresent(conversation -> {
            if (isDoctor) {
                conversation.setDoctorUnreadCount(
                        (conversation.getDoctorUnreadCount() != null ? conversation.getDoctorUnreadCount() : 0) + 1
                );
            } else {
                conversation.setParentUnreadCount(
                        (conversation.getParentUnreadCount() != null ? conversation.getParentUnreadCount() : 0) + 1
                );
            }
            conversationRepository.save(conversation);
        });
    }

    public void markAsRead(String conversationId, boolean isDoctor) {
        conversationRepository.findById(conversationId).ifPresent(conversation -> {
            if (isDoctor) {
                conversation.setDoctorUnreadCount(0);
            } else {
                conversation.setParentUnreadCount(0);
            }
            conversationRepository.save(conversation);
        });
    }

    private ConversationResponse convertToResponse(Conversation conversation) {
        return ConversationResponse.builder()
                .id(conversation.getId())
                .doctorId(conversation.getDoctorId())
                .parentId(conversation.getParentId())
                .childId(conversation.getChildId())
                .subject(conversation.getSubject())
                .contextType(conversation.getContextType())
                .contextId(conversation.getContextId())
                .lastMessageContent(conversation.getLastMessageContent())
                .lastMessageTime(conversation.getLastMessageTime())
                .lastMessageSenderId(conversation.getLastMessageSenderId())
                .doctorUnreadCount(conversation.getDoctorUnreadCount())
                .parentUnreadCount(conversation.getParentUnreadCount())
                .status(conversation.getStatus())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }
}
