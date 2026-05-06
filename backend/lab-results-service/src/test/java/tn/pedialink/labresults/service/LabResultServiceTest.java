package tn.pedialink.labresults.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.pedialink.labresults.dto.CreateLabResultRequest;
import tn.pedialink.labresults.dto.LabResultDto;
import tn.pedialink.labresults.entity.CKDStage;
import tn.pedialink.labresults.entity.LabResult;
import tn.pedialink.labresults.entity.ResultStatus;
import tn.pedialink.labresults.entity.TestType;
import tn.pedialink.labresults.repository.LabResultRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du service de résultats de laboratoire")
class LabResultServiceTest {

    @Mock
    private LabResultRepository labResultRepository;

    @Mock
    private EGFRCalculationService egfrCalculationService;

    @Mock
    private AlertService alertService;

    @Mock
    private EmailNotificationService emailNotificationService;

    @InjectMocks
    private LabResultService labResultService;

    private LabResult sampleLabResult;
    private CreateLabResultRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleLabResult = new LabResult();
        sampleLabResult.setId("lab-001");
        sampleLabResult.setPatientId("patient-001");
        sampleLabResult.setDoctorId("doctor-001");
        sampleLabResult.setTestType(TestType.BLOOD);
        sampleLabResult.setTestDate(LocalDateTime.now());
        sampleLabResult.setCreatinine(0.8);
        sampleLabResult.setStatus(ResultStatus.PENDING);
        sampleLabResult.setIsAbnormal(false);

        sampleRequest = new CreateLabResultRequest();
        sampleRequest.setPatientId("patient-001");
        sampleRequest.setTestType(TestType.BLOOD);
        sampleRequest.setTestDate(LocalDateTime.now());
        sampleRequest.setCreatinine(0.8);
        sampleRequest.setSendEmailNotification(false);
    }

    // ===== Tests création =====

    @Test
    @DisplayName("Création d'un résultat de laboratoire avec calcul eGFR automatique")
    void createLabResult_withCreatinine_calculatesEGFR() {
        when(egfrCalculationService.calculateEGFR(anyDouble(), anyDouble(), anyBoolean())).thenReturn(85.0);
        when(egfrCalculationService.determineCKDStage(85.0)).thenReturn(CKDStage.STAGE_1);
        when(alertService.generateAlerts(any())).thenReturn(List.of());
        when(alertService.hasAbnormalValues(any())).thenReturn(false);
        when(labResultRepository.save(any())).thenReturn(sampleLabResult);

        LabResultDto result = labResultService.createLabResult(sampleRequest, "doctor-001");

        assertNotNull(result);
        verify(egfrCalculationService).calculateEGFR(anyDouble(), anyDouble(), anyBoolean());
        verify(labResultRepository).save(any(LabResult.class));
    }

    @Test
    @DisplayName("Création génère des alertes si valeurs anormales")
    void createLabResult_abnormalValues_generatesAlerts() {
        sampleRequest.setPotassium(7.0);
        List<String> expectedAlerts = List.of("URGENT: Potassium dangerously high");

        when(egfrCalculationService.calculateEGFR(anyDouble(), anyDouble(), anyBoolean())).thenReturn(85.0);
        when(egfrCalculationService.determineCKDStage(anyDouble())).thenReturn(CKDStage.STAGE_1);
        when(alertService.generateAlerts(any())).thenReturn(expectedAlerts);
        when(alertService.hasAbnormalValues(any())).thenReturn(true);
        when(labResultRepository.save(any())).thenAnswer(inv -> {
            LabResult lr = inv.getArgument(0);
            lr.setAlerts(expectedAlerts);
            lr.setIsAbnormal(true);
            return lr;
        });

        LabResultDto result = labResultService.createLabResult(sampleRequest, "doctor-001");

        assertNotNull(result);
        verify(alertService).generateAlerts(any());
    }

    // ===== Tests récupération =====

    @Test
    @DisplayName("Récupération des résultats par patient")
    void getLabResultsByPatient_returnsResults() {
        List<LabResult> results = Arrays.asList(sampleLabResult);
        when(labResultRepository.findByPatientIdOrderByTestDateDesc("patient-001")).thenReturn(results);

        List<LabResultDto> dtos = labResultService.getLabResultsByPatient("patient-001");

        assertNotNull(dtos);
        assertEquals(1, dtos.size());
        verify(labResultRepository).findByPatientIdOrderByTestDateDesc("patient-001");
    }

    @Test
    @DisplayName("Récupération par ID - résultat trouvé")
    void getLabResultById_existingId_returnsResult() {
        when(labResultRepository.findById("lab-001")).thenReturn(Optional.of(sampleLabResult));

        LabResultDto result = labResultService.getLabResultById("lab-001");

        assertNotNull(result);
        assertEquals("lab-001", result.getId());
    }

    @Test
    @DisplayName("Récupération par ID - résultat non trouvé lance exception")
    void getLabResultById_nonExistingId_throwsException() {
        when(labResultRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> labResultService.getLabResultById("unknown"));
    }

    // ===== Tests validation =====

    @Test
    @DisplayName("Validation d'un résultat change le statut à VALIDATED")
    void validateLabResult_pendingResult_changesStatusToValidated() {
        when(labResultRepository.findById("lab-001")).thenReturn(Optional.of(sampleLabResult));
        when(labResultRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LabResultDto result = labResultService.validateLabResult("lab-001", "doctor-001");

        assertNotNull(result);
        verify(labResultRepository).save(argThat(lr -> lr.getStatus() == ResultStatus.VALIDATED));
    }

    // ===== Tests suppression =====

    @Test
    @DisplayName("Suppression d'un résultat existant")
    void deleteLabResult_existingId_deletesSuccessfully() {
        doNothing().when(labResultRepository).deleteById("lab-001");

        assertDoesNotThrow(() -> labResultService.deleteLabResult("lab-001"));
        verify(labResultRepository).deleteById("lab-001");
    }
}
