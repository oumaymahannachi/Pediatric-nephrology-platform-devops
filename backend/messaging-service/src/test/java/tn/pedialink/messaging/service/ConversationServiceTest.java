package tn.pedialink.messaging.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.pedialink.messaging.dto.ConversationResponse;
import tn.pedialink.messaging.dto.CreateConversationRequest;
import tn.pedialink.messaging.model.Conversation;
import tn.pedialink.messaging.repository.ConversationRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour ConversationService avec Mockito
 */
@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @InjectMocks
    private ConversationService conversationService;

    private Conversation sampleConversation;
    private CreateConversationRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleConversation = new Conversation();
        sampleConversation.setId("conv-001");
        sampleConversation.setDoctorId("doctor-001");
        sampleConversation.setParentId("parent-001");
        sampleConversation.setChildId("child-001");
        sampleConversation.setSubject("Question sur le traitement");
        sampleConversation.setDoctorUnreadCount(0);
        sampleConversation.setParentUnreadCount(0);
        sampleConversation.setStatus(Conversation.ConversationStatus.ACTIVE);

        sampleRequest = new CreateConversationRequest();
        sampleRequest.setDoctorId("doctor-001");
        sampleRequest.setParentId("parent-001");
        sampleRequest.setChildId("child-001");
        sampleRequest.setSubject("Question sur le traitement");
    }

    // ===== TESTS CRÉATION =====

    @Test
    @DisplayName("Création d'une nouvelle conversation")
    void testCreateConversation_New() {
        when(conversationRepository.findByDoctorIdAndParentIdAndChildId(
                anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(conversationRepository.save(any(Conversation.class)))
                .thenReturn(sampleConversation);

        ConversationResponse response = conversationService.createConversation(sampleRequest);

        assertNotNull(response);
        assertEquals("conv-001", response.getId());
        assertEquals("doctor-001", response.getDoctorId());
        verify(conversationRepository, times(1)).save(any(Conversation.class));
    }

    @Test
    @DisplayName("Conversation existante - retourne l'existante sans créer")
    void testCreateConversation_AlreadyExists() {
        when(conversationRepository.findByDoctorIdAndParentIdAndChildId(
                anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(sampleConversation));

        ConversationResponse response = conversationService.createConversation(sampleRequest);

        assertNotNull(response);
        assertEquals("conv-001", response.getId());
        // Ne doit PAS sauvegarder une nouvelle conversation
        verify(conversationRepository, never()).save(any(Conversation.class));
    }

    // ===== TESTS RÉCUPÉRATION =====

    @Test
    @DisplayName("Récupération des conversations du médecin")
    void testGetDoctorConversations() {
        when(conversationRepository.findByDoctorIdOrderByLastMessageTimeDesc("doctor-001"))
                .thenReturn(Arrays.asList(sampleConversation));

        List<ConversationResponse> responses = conversationService.getDoctorConversations("doctor-001");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("doctor-001", responses.get(0).getDoctorId());
    }

    @Test
    @DisplayName("Récupération des conversations du parent")
    void testGetParentConversations() {
        when(conversationRepository.findByParentIdOrderByLastMessageTimeDesc("parent-001"))
                .thenReturn(Arrays.asList(sampleConversation));

        List<ConversationResponse> responses = conversationService.getParentConversations("parent-001");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("parent-001", responses.get(0).getParentId());
    }

    // ===== TESTS MISE À JOUR =====

    @Test
    @DisplayName("Mise à jour du dernier message")
    void testUpdateLastMessage() {
        when(conversationRepository.findById("conv-001"))
                .thenReturn(Optional.of(sampleConversation));
        when(conversationRepository.save(any())).thenReturn(sampleConversation);

        assertDoesNotThrow(() ->
                conversationService.updateLastMessage("conv-001", "Nouveau message", "doctor-001"));

        verify(conversationRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Incrémenter compteur non lus du docteur")
    void testIncrementUnreadCount_Doctor() {
        when(conversationRepository.findById("conv-001"))
                .thenReturn(Optional.of(sampleConversation));
        when(conversationRepository.save(any())).thenReturn(sampleConversation);

        conversationService.incrementUnreadCount("conv-001", true);

        assertEquals(1, sampleConversation.getDoctorUnreadCount());
        verify(conversationRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Incrémenter compteur non lus du parent")
    void testIncrementUnreadCount_Parent() {
        when(conversationRepository.findById("conv-001"))
                .thenReturn(Optional.of(sampleConversation));
        when(conversationRepository.save(any())).thenReturn(sampleConversation);

        conversationService.incrementUnreadCount("conv-001", false);

        assertEquals(1, sampleConversation.getParentUnreadCount());
    }
}
