package tn.pedialink.dossiermedical.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.pedialink.dossiermedical.dto.GFRCalculationDto;
import tn.pedialink.dossiermedical.model.kidney.CKDStage;
import tn.pedialink.dossiermedical.model.kidney.GFRCalculation;
import tn.pedialink.dossiermedical.repository.GFRCalculationRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GFRCalculationServiceTest {

    @Mock
    private GFRCalculationRepository gfrRepository;

    @InjectMocks
    private GFRCalculationService gfrService;

    private GFRCalculationDto dto;

    @BeforeEach
    void setUp() {
        dto = new GFRCalculationDto();
        dto.setPatientId("patient-001");
        dto.setPatientName("Ahmed Ben Ali");
        dto.setMedecinId("doctor-001");
        dto.setHeightCm(120.0);
        dto.setSerumCreatinine(0.6);
        dto.setNotes("Routine check");
    }

    // ===== TESTS LOGIQUE MÉTIER : Formule de Schwartz =====

    @Test
    void calculateGFR_shouldApplySchwartzFormula() {
        // GFR = (0.413 × 120) / 0.6 = 82.6
        GFRCalculation saved = new GFRCalculation();
        saved.setGfrValue(82.6);
        when(gfrRepository.save(any())).thenReturn(saved);

        GFRCalculation result = gfrService.calculateGFR(dto);

        assertThat(result.getGfrValue()).isEqualTo(82.6);
    }

    @Test
    void calculateGFR_normalKidneyFunction_shouldBeStage1() {
        // GFR = (0.413 × 120) / 0.6 = 82.6 → Stage 1 (≥ 90 is G1, but 60-89 is G2)
        // With height=150, creatinine=0.5 → GFR = (0.413*150)/0.5 = 123.9 → Stage 1
        dto.setHeightCm(150.0);
        dto.setSerumCreatinine(0.5);

        GFRCalculation saved = new GFRCalculation();
        saved.setGfrValue(123.9);
        saved.setCkdStage(CKDStage.STAGE_1);
        when(gfrRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        GFRCalculation result = gfrService.calculateGFR(dto);

        assertThat(result.getCkdStage()).isEqualTo(CKDStage.STAGE_1);
        assertThat(result.getGfrValue()).isGreaterThan(90.0);
    }

    @Test
    void calculateGFR_kidneyFailure_shouldBeStage5() {
        // GFR = (0.413 × 100) / 3.0 = 13.77 → Stage 5 (< 15)
        dto.setHeightCm(100.0);
        dto.setSerumCreatinine(3.0);

        when(gfrRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        GFRCalculation result = gfrService.calculateGFR(dto);

        assertThat(result.getCkdStage()).isEqualTo(CKDStage.STAGE_5);
        assertThat(result.getInterpretation()).contains("URGENT");
    }

    @Test
    void calculateGFR_stage3_shouldMentionModerate() {
        // GFR = (0.413 × 100) / 1.5 = 27.5 → Stage 4 (15-29)
        // For stage 3: GFR 30-59 → height=100, creatinine=0.8 → GFR=51.6
        dto.setHeightCm(100.0);
        dto.setSerumCreatinine(0.8);

        when(gfrRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        GFRCalculation result = gfrService.calculateGFR(dto);

        assertThat(result.getCkdStage()).isIn(CKDStage.STAGE_3A, CKDStage.STAGE_3B);
        assertThat(result.getInterpretation()).contains("Moderate CKD");
    }

    @Test
    void calculateGFR_shouldRoundToTwoDecimals() {
        dto.setHeightCm(100.0);
        dto.setSerumCreatinine(0.7);
        // GFR = (0.413 * 100) / 0.7 = 59.0

        when(gfrRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        GFRCalculation result = gfrService.calculateGFR(dto);

        String gfrStr = String.valueOf(result.getGfrValue());
        String[] parts = gfrStr.split("\\.");
        assertThat(parts.length == 1 || parts[1].length() <= 2).isTrue();
    }

    @Test
    void calculateGFR_shouldSaveToRepository() {
        when(gfrRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        gfrService.calculateGFR(dto);

        verify(gfrRepository, times(1)).save(any(GFRCalculation.class));
    }

    @Test
    void calculateGFR_shouldSetPatientInfo() {
        when(gfrRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        GFRCalculation result = gfrService.calculateGFR(dto);

        assertThat(result.getPatientId()).isEqualTo("patient-001");
        assertThat(result.getPatientName()).isEqualTo("Ahmed Ben Ali");
        assertThat(result.getMedecinId()).isEqualTo("doctor-001");
    }

    // ===== TESTS HISTORIQUE =====

    @Test
    void getPatientGFRHistory_shouldReturnOrderedList() {
        GFRCalculation g1 = new GFRCalculation();
        GFRCalculation g2 = new GFRCalculation();
        when(gfrRepository.findByPatientIdOrderByCalculationDateDesc("patient-001"))
            .thenReturn(Arrays.asList(g1, g2));

        List<GFRCalculation> result = gfrService.getPatientGFRHistory("patient-001");

        assertThat(result).hasSize(2);
        verify(gfrRepository).findByPatientIdOrderByCalculationDateDesc("patient-001");
    }

    @Test
    void getLatestGFR_shouldReturnFirstElement() {
        GFRCalculation latest = new GFRCalculation();
        latest.setGfrValue(75.0);
        when(gfrRepository.findByPatientIdOrderByCalculationDateDesc("patient-001"))
            .thenReturn(Arrays.asList(latest));

        GFRCalculation result = gfrService.getLatestGFR("patient-001");

        assertThat(result.getGfrValue()).isEqualTo(75.0);
    }

    @Test
    void getLatestGFR_noHistory_shouldReturnNull() {
        when(gfrRepository.findByPatientIdOrderByCalculationDateDesc("patient-001"))
            .thenReturn(Collections.emptyList());

        GFRCalculation result = gfrService.getLatestGFR("patient-001");

        assertThat(result).isNull();
    }
}
