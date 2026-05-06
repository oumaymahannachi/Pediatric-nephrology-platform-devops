package tn.pedialink.labresults.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import tn.pedialink.labresults.entity.LabResult;
import tn.pedialink.labresults.entity.TestType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests du service d'alertes médicales")
class AlertServiceTest {

    private AlertService alertService;

    @BeforeEach
    void setUp() {
        alertService = new AlertService();
    }

    private LabResult createLabResult() {
        LabResult result = new LabResult();
        result.setTestType(TestType.BLOOD);
        return result;
    }

    // ===== Tests potassium =====

    @Test
    @DisplayName("Alerte URGENT - potassium dangereux > 6.0")
    void generateAlerts_criticalPotassium_returnsUrgentAlert() {
        LabResult result = createLabResult();
        result.setPotassium(6.5);

        List<String> alerts = alertService.generateAlerts(result);

        assertTrue(alerts.stream().anyMatch(a -> a.contains("URGENT")));
        assertTrue(alerts.stream().anyMatch(a -> a.toLowerCase().contains("potassium")));
    }

    @Test
    @DisplayName("Alerte WARNING - potassium élevé entre 5.5 et 6.0")
    void generateAlerts_highPotassium_returnsWarning() {
        LabResult result = createLabResult();
        result.setPotassium(5.8);

        List<String> alerts = alertService.generateAlerts(result);

        assertTrue(alerts.stream().anyMatch(a -> a.contains("WARNING") && a.toLowerCase().contains("potassium")));
    }

    @Test
    @DisplayName("Pas d'alerte - potassium normal")
    void generateAlerts_normalPotassium_noAlert() {
        LabResult result = createLabResult();
        result.setPotassium(4.0);

        List<String> alerts = alertService.generateAlerts(result);

        assertTrue(alerts.stream().noneMatch(a -> a.toLowerCase().contains("potassium")));
    }

    // ===== Tests hémoglobine =====

    @Test
    @DisplayName("Alerte URGENT - anémie sévère Hb < 7.0")
    void generateAlerts_severeAnemia_returnsUrgentAlert() {
        LabResult result = createLabResult();
        result.setHemoglobin(6.0);

        List<String> alerts = alertService.generateAlerts(result);

        assertTrue(alerts.stream().anyMatch(a -> a.contains("URGENT") && a.toLowerCase().contains("anemia")));
    }

    @Test
    @DisplayName("Alerte WARNING - anémie modérée Hb entre 7 et 10")
    void generateAlerts_moderateAnemia_returnsWarning() {
        LabResult result = createLabResult();
        result.setHemoglobin(8.5);

        List<String> alerts = alertService.generateAlerts(result);

        assertTrue(alerts.stream().anyMatch(a -> a.contains("WARNING") && a.toLowerCase().contains("anemia")));
    }

    // ===== Tests eGFR =====

    @Test
    @DisplayName("Alerte CRITICAL - insuffisance rénale terminale eGFR < 15")
    void generateAlerts_kidneyFailure_returnsCriticalAlert() {
        LabResult result = createLabResult();
        result.setEGFR(10.0);

        List<String> alerts = alertService.generateAlerts(result);

        assertTrue(alerts.stream().anyMatch(a -> a.contains("CRITICAL")));
    }

    @Test
    @DisplayName("Alerte SEVERE - eGFR entre 15 et 30")
    void generateAlerts_severeKidneyDisease_returnsSevereAlert() {
        LabResult result = createLabResult();
        result.setEGFR(25.0);

        List<String> alerts = alertService.generateAlerts(result);

        assertTrue(alerts.stream().anyMatch(a -> a.contains("SEVERE")));
    }

    // ===== Tests hasAbnormalValues =====

    @Test
    @DisplayName("Valeurs anormales détectées")
    void hasAbnormalValues_withAlerts_returnsTrue() {
        LabResult result = createLabResult();
        result.setPotassium(7.0);

        assertTrue(alertService.hasAbnormalValues(result));
    }

    @Test
    @DisplayName("Valeurs normales - pas d'anomalie")
    void hasAbnormalValues_normalValues_returnsFalse() {
        LabResult result = createLabResult();
        result.setPotassium(4.0);
        result.setSodium(140.0);
        result.setHemoglobin(12.0);

        assertFalse(alertService.hasAbnormalValues(result));
    }

    @Test
    @DisplayName("Résultat sans valeurs - pas d'alerte")
    void generateAlerts_emptyResult_returnsEmptyList() {
        LabResult result = createLabResult();

        List<String> alerts = alertService.generateAlerts(result);

        assertTrue(alerts.isEmpty());
    }
}
