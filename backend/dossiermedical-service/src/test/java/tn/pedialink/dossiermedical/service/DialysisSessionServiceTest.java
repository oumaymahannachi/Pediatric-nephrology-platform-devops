package tn.pedialink.dossiermedical.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.pedialink.dossiermedical.dto.DialysisSessionDto;
import tn.pedialink.dossiermedical.model.dialyse.DialysisSession;
import tn.pedialink.dossiermedical.model.dialyse.StatutSession;
import tn.pedialink.dossiermedical.repository.DialysisSessionRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DialysisSessionServiceTest {

    @Mock
    private DialysisSessionRepository sessionRepository;

    @InjectMocks
    private DialysisSessionService dialysisService;

    private DialysisSessionDto dto;
    private DialysisSession session;

    @BeforeEach
    void setUp() {
        dto = new DialysisSessionDto();
        dto.setPatientId("patient-001");
        dto.setPatientName("Mohamed Slim");
        dto.setMedecinId("doctor-001");
        dto.setPrescriptionId("presc-001");
        dto.setScheduledDate(LocalDateTime.now().plusDays(1));

        session = new DialysisSession();
        session.setId("session-001");
        session.setPatientId("patient-001");
        session.setStatus(StatutSession.SCHEDULED);
    }

    // ===== TESTS LOGIQUE MÉTIER : Cycle de vie d'une session =====

    @Test
    void scheduleSession_shouldCreateWithScheduledStatus() {
        when(sessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DialysisSession result = dialysisService.scheduleSession(dto);

        assertThat(result.getStatus()).isEqualTo(StatutSession.SCHEDULED);
        assertThat(result.getPatientId()).isEqualTo("patient-001");
    }

    @Test
    void startSession_shouldChangeStatusToInProgress() {
        when(sessionRepository.findById("session-001")).thenReturn(Optional.of(session));
        when(sessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DialysisSession result = dialysisService.startSession("session-001");

        assertThat(result.getStatus()).isEqualTo(StatutSession.IN_PROGRESS);
        assertThat(result.getStartTime()).isNotNull();
    }

    @Test
    void completeSession_shouldChangeStatusToCompleted() {
        session.setStatus(StatutSession.IN_PROGRESS);
        dto.setPreWeight(45.0);
        dto.setPostWeight(43.5);
        dto.setUltrafiltrationVolume(1500.0);

        when(sessionRepository.findById("session-001")).thenReturn(Optional.of(session));
        when(sessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DialysisSession result = dialysisService.completeSession("session-001", dto);

        assertThat(result.getStatus()).isEqualTo(StatutSession.COMPLETED);
        assertThat(result.getEndTime()).isNotNull();
        assertThat(result.getPreWeight()).isEqualTo(45.0);
        assertThat(result.getPostWeight()).isEqualTo(43.5);
    }

    @Test
    void cancelSession_shouldChangeStatusToCancelled() {
        when(sessionRepository.findById("session-001")).thenReturn(Optional.of(session));
        when(sessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DialysisSession result = dialysisService.cancelSession("session-001", "Patient unavailable");

        assertThat(result.getStatus()).isEqualTo(StatutSession.CANCELLED);
        assertThat(result.getNotes()).isEqualTo("Patient unavailable");
    }

    @Test
    void rescheduleSession_shouldUpdateDateAndKeepScheduledStatus() {
        LocalDateTime newDate = LocalDateTime.now().plusDays(3);
        when(sessionRepository.findById("session-001")).thenReturn(Optional.of(session));
        when(sessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DialysisSession result = dialysisService.rescheduleSession("session-001", newDate);

        assertThat(result.getScheduledDate()).isEqualTo(newDate);
        assertThat(result.getStatus()).isEqualTo(StatutSession.SCHEDULED);
    }

    // ===== TESTS NOT FOUND =====

    @Test
    void startSession_notFound_shouldThrowException() {
        when(sessionRepository.findById("invalid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dialysisService.startSession("invalid"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Session not found");
    }

    @Test
    void completeSession_notFound_shouldThrowException() {
        when(sessionRepository.findById("invalid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dialysisService.completeSession("invalid", dto))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Session not found");
    }

    @Test
    void cancelSession_notFound_shouldThrowException() {
        when(sessionRepository.findById("invalid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dialysisService.cancelSession("invalid", "reason"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Session not found");
    }

    // ===== TESTS QUERIES =====

    @Test
    void getSessionsByPatient_shouldReturnList() {
        when(sessionRepository.findByPatientId("patient-001"))
            .thenReturn(Arrays.asList(session));

        var result = dialysisService.getSessionsByPatient("patient-001");

        assertThat(result).hasSize(1);
    }

    @Test
    void getSessionsByDateRange_shouldCallRepository() {
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();
        when(sessionRepository.findByScheduledDateBetween(start, end))
            .thenReturn(Arrays.asList(session));

        var result = dialysisService.getSessionsByDateRange(start, end);

        assertThat(result).hasSize(1);
        verify(sessionRepository).findByScheduledDateBetween(start, end);
    }
}
