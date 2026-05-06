package tn.pedialink.labresults.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import tn.pedialink.labresults.entity.CKDStage;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests du service de calcul eGFR")
class EGFRCalculationServiceTest {

    private EGFRCalculationService service;

    @BeforeEach
    void setUp() {
        service = new EGFRCalculationService();
    }

    // ===== Tests calcul eGFR =====

    @Test
    @DisplayName("Calcul eGFR normal - enfant avec créatinine normale")
    void calculateEGFR_normalCreatinine_returnsNormalValue() {
        Double egfr = service.calculateEGFR(0.5, 120.0, true);
        assertNotNull(egfr);
        assertTrue(egfr > 60, "eGFR devrait être > 60 pour une créatinine normale");
    }

    @Test
    @DisplayName("Calcul eGFR - créatinine élevée indique insuffisance rénale")
    void calculateEGFR_highCreatinine_returnsLowValue() {
        Double egfr = service.calculateEGFR(5.0, 120.0, true);
        assertNotNull(egfr);
        assertTrue(egfr < 30, "eGFR devrait être < 30 pour une créatinine très élevée");
    }

    @Test
    @DisplayName("Calcul eGFR - créatinine nulle retourne null")
    void calculateEGFR_nullCreatinine_returnsNull() {
        Double egfr = service.calculateEGFR(null, 120.0, true);
        assertNull(egfr, "eGFR devrait être null si créatinine est null");
    }

    @Test
    @DisplayName("Calcul eGFR - taille nulle retourne null")
    void calculateEGFR_nullHeight_returnsNull() {
        Double egfr = service.calculateEGFR(0.5, null, true);
        assertNull(egfr, "eGFR devrait être null si taille est null");
    }

    // ===== Tests stade CKD =====

    @Test
    @DisplayName("Stade CKD 1 - eGFR >= 90")
    void determineCKDStage_eGFR90_returnsStage1() {
        CKDStage stage = service.determineCKDStage(95.0);
        assertEquals(CKDStage.STAGE_1, stage);
    }

    @Test
    @DisplayName("Stade CKD 2 - eGFR entre 60 et 89")
    void determineCKDStage_eGFR75_returnsStage2() {
        CKDStage stage = service.determineCKDStage(75.0);
        assertEquals(CKDStage.STAGE_2, stage);
    }

    @Test
    @DisplayName("Stade CKD 3A - eGFR entre 45 et 59")
    void determineCKDStage_eGFR50_returnsStage3A() {
        CKDStage stage = service.determineCKDStage(50.0);
        assertEquals(CKDStage.STAGE_3A, stage);
    }

    @Test
    @DisplayName("Stade CKD 3B - eGFR entre 30 et 44")
    void determineCKDStage_eGFR35_returnsStage3B() {
        CKDStage stage = service.determineCKDStage(35.0);
        assertEquals(CKDStage.STAGE_3B, stage);
    }

    @Test
    @DisplayName("Stade CKD 4 - eGFR entre 15 et 29")
    void determineCKDStage_eGFR20_returnsStage4() {
        CKDStage stage = service.determineCKDStage(20.0);
        assertEquals(CKDStage.STAGE_4, stage);
    }

    @Test
    @DisplayName("Stade CKD 5 - eGFR < 15 - insuffisance rénale terminale")
    void determineCKDStage_eGFR10_returnsStage5() {
        CKDStage stage = service.determineCKDStage(10.0);
        assertEquals(CKDStage.STAGE_5, stage);
    }

    @Test
    @DisplayName("Stade CKD inconnu - eGFR null")
    void determineCKDStage_nullEGFR_returnsUnknown() {
        CKDStage stage = service.determineCKDStage(null);
        assertEquals(CKDStage.UNKNOWN, stage);
    }

    // ===== Tests ratio protéine/créatinine =====

    @Test
    @DisplayName("Calcul ratio protéine/créatinine normal")
    void calculateProteinCreatinineRatio_validValues_returnsRatio() {
        // 300 mg/L / 100 mg/dL = 3.0 (ratio significatif)
        Double ratio = service.calculateProteinCreatinineRatio(300.0, 100.0);
        assertNotNull(ratio);
        assertEquals(3.0, ratio, 0.01);
    }

    @Test
    @DisplayName("Calcul ratio - créatinine urinaire nulle retourne null")
    void calculateProteinCreatinineRatio_nullCreatinine_returnsNull() {
        Double ratio = service.calculateProteinCreatinineRatio(0.3, null);
        assertNull(ratio);
    }
}
