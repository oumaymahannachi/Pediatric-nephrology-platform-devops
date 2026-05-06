package tn.pedialink.messaging.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.pedialink.messaging.dto.MessageResponse;
import tn.pedialink.messaging.dto.SendMessageRequest;
import tn.pedialink.messaging.model.Message;
import tn.pedialink.messaging.repository.MessageRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour MessageService avec Mockito
 */
@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ConversationService conversationService;

    @InjectMocks
    private MessageService messageService;

    private Message sampleMessage;
    private SendMessageRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleMessage = new Message();
        sampleMessage.setId("msg-001");
        sampleMessage.setConversationId("conv-001");
        sampleMessage.setSenderId("doctor-001");
        sampleMessage.setSenderType(Message.SenderType.DOCTOR);
        sampleMessage.setContent("Bonjour, comment va votre enfant ?");
        sampleMessage.setIsRead(false);
        sampleMessage.setCreatedAt(LocalDateTime.now());

        sampleRequest = new SendMessageRequest();
        sampleRequest.setConversationId("conv-001");
        sampleRequest.setSenderId("doctor-001");
        sampleRequest.setSenderType(Message.SenderType.DOCTOR);
        sampleRequest.setContent("Bonjour, comment va votre enfant ?");
    }

    // ===== TESTS ENVOI DE MESSAGE =====

    @Test
    @DisplayName("Envoi d'un message - sauvegarde et mise à jour conversation")
    void testSendMessage_Success() {
        when(messageRepository.save(any(Message.class))).thenReturn(sampleMessage);
        doNothing().when(conversationService).updateLastMessage(anyString(), anyString(), anyString());
        doNothing().when(conversationService).incrementUnreadCount(anyString(), anyBoolean());

        MessageResponse response = messageService.sendMessage(sampleRequest);

        assertNotNull(response);
        assertEquals("conv-001", response.getConversationId());
        assertEquals("doctor-001", response.getSenderId());
        assertEquals("Bonjour, comment va votre enfant ?", response.getContent());
        assertFalse(response.getIsRead());

        verify(messageRepository, times(1)).save(any(Message.class));
        verify(conversationService, times(1)).updateLastMessage(anyString(), anyString(), anyString());
        verify(conversationService, times(1)).incrementUnreadCount(anyString(), anyBoolean());
    }

    @Test
    @DisplayName("Envoi message PARENT - incrémente unread du docteur")
    void testSendMessage_FromParent_IncrementsDocUnread() {
        sampleRequest.setSenderType(Message.SenderType.PARENT);
        sampleMessage.setSenderType(Message.SenderType.PARENT);

        when(messageRepository.save(any())).thenReturn(sampleMessage);
        doNothing().when(conversationService).updateLastMessage(any(), any(), any());
        doNothing().when(conversationService).incrementUnreadCount(anyString(), anyBoolean());

        messageService.sendMessage(sampleRequest);

        // PARENT envoie → isDoctor=true (unread pour le docteur)
        verify(conversationService).incrementUnreadCount("conv-001", true);
    }

    @Test
    @DisplayName("Envoi message DOCTOR - incrémente unread du parent")
    void testSendMessage_FromDoctor_IncrementsParentUnread() {
        when(messageRepository.save(any())).thenReturn(sampleMessage);
        doNothing().when(conversationService).updateLastMessage(any(), any(), any());
        doNothing().when(conversationService).incrementUnreadCount(anyString(), anyBoolean());

        messageService.sendMessage(sampleRequest);

        // DOCTOR envoie → isDoctor=false (unread pour le parent)
        verify(conversationService).incrementUnreadCount("conv-001", false);
    }

    // ===== TESTS RÉCUPÉRATION =====

    @Test
    @DisplayName("Récupération des messages d'une conversation")
    void testGetConversationMessages() {
        List<Message> messages = Arrays.asList(sampleMessage);
        when(messageRepository.findByConversationIdOrderByCreatedAtAsc("conv-001"))
                .thenReturn(messages);

        List<MessageResponse> responses = messageService.getConversationMessages("conv-001");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("msg-001", responses.get(0).getId());
    }

    @Test
    @DisplayName("Conversation vide retourne liste vide")
    void testGetConversationMessages_Empty() {
        when(messageRepository.findByConversationIdOrderByCreatedAtAsc("conv-empty"))
                .thenReturn(List.of());

        List<MessageResponse> responses = messageService.getConversationMessages("conv-empty");

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    // ===== TESTS MARQUER COMME LU =====

    @Test
    @DisplayName("Marquer les messages comme lus - seulement les messages de l'autre")
    void testMarkMessagesAsRead() {
        Message unreadMessage = new Message();
        unreadMessage.setId("msg-002");
        unreadMessage.setSenderId("parent-001"); // envoyé par le parent
        unreadMessage.setIsRead(false);

        when(messageRepository.findByConversationIdAndIsReadFalse("conv-001"))
                .thenReturn(Arrays.asList(unreadMessage));
        when(messageRepository.saveAll(any())).thenReturn(List.of());

        messageService.markMessagesAsRead("conv-001", "doctor-001");

        // Le message du parent doit être marqué comme lu par le docteur
        assertTrue(unreadMessage.getIsRead());
        assertNotNull(unreadMessage.getReadAt());
        verify(messageRepository, times(1)).saveAll(any());
    }

    @Test
    @DisplayName("Ne pas marquer ses propres messages comme lus")
    void testMarkMessagesAsRead_OwnMessages() {
        Message ownMessage = new Message();
        ownMessage.setId("msg-003");
        ownMessage.setSenderId("doctor-001"); // envoyé par le docteur lui-même
        ownMessage.setIsRead(false);

        when(messageRepository.findByConversationIdAndIsReadFalse("conv-001"))
                .thenReturn(Arrays.asList(ownMessage));
        when(messageRepository.saveAll(any())).thenReturn(List.of());

        messageService.markMessagesAsRead("conv-001", "doctor-001");

        // Son propre message ne doit PAS être marqué comme lu
        assertFalse(ownMessage.getIsRead());
    }

    // ===== TESTS COMPTEUR NON LUS =====

    @Test
    @DisplayName("Compteur de messages non lus")
    void testGetUnreadCount() {
        when(messageRepository.countByConversationIdAndIsReadFalse("conv-001")).thenReturn(5L);

        Long count = messageService.getUnreadCount("conv-001");

        assertEquals(5L, count);
    }
}
