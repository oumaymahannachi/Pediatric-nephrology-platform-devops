package tn.pedialink.dossiermedical.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.pedialink.dossiermedical.model.analytics.ClinicalAlert;
import tn.pedialink.dossiermedical.model.analytics.ClinicalAlert.AlertSeverity;
import tn.pedialink.dossiermedical.model.analytics.ClinicalAlert.AlertType;
import tn.pedialink.dossiermedical.model.examen.BloodTest;
import tn.pedialink.dossiermedical.model.examen.LabResult;
import tn.pedialink.dossiermedical.model.kidney.CKDStage;
import tn.pedialink.dossiermedical.model.kidney.GFRCalculation;
import tn.pedialink.dossiermedical.repository.ClinicalAlertRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service de détection automatique des alertes cliniques.
 * Analyse les résultats d'examens et génère des alertes selon des seuils pédiatriques.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClinicalAlertService {

    private final ClinicalAlertRepository alertRepository;

    // Seuils critiques pédiatriques (néphropédiatrie)
    private static final Map<String, double[]> CRITICAL_THRESHOLDS = Map.of(
        "creatinine",    new double[]{0.0, 2.0},   // > 2.0 mg/dL = critique
        "potassium",     new double[]{3.0, 6.0},   // < 3.0 ou > 6.0 mEq/L
        "hemoglobin",    new double[]{7.0, 20.0},  // < 7.0 g/dL = anémie sévère
        "sodium",        new double[]{125.0, 155.0}, // hors plage = critique
        "bicarbonate",   new double[]{15.0, 35.0}, // acidose/alcalose
        "phosphorus",    new double[]{1.0, 8.0},   // hyperphosphatémie
        "urea",          new double[]{0.0, 150.0}  // > 150 mg/dL = urémie sévère
    );

    /**
     * Analyse un bilan sanguin et génère les alertes appropriées.
     */
    public List<ClinicalAlert> analyzeBloodTest(BloodTest bloodTest) {
        List<ClinicalAlert> alerts = new ArrayList<>();

        if (bloodTest.getResults() == null) return alerts;

        bloodTest.getResults().forEach((testName, testValue) -> {
            if (testValue.getValue() == null) return;

            double value = testValue.getValue();
            String key = testName.toLowerCase();

            CRITICAL_THRESHOLDS.forEach((param, range) -> {
                if (key.contains(param)) {
                    if (value < range[0] || value > range[1]) {
                        ClinicalAlert alert = buildAlert(
                            bloodTest.getPatientId(),
                            bloodTest.getPatientName(),
                            bloodTest.getMedecinId(),
                            AlertType.CRITICAL_VALUE,
                            determineSeverity(param, value, range),
                            "Valeur critique: " + testName,
                            String.format("Valeur %s = %.2f %s (plage normale: %.1f - %.1f)",
                                testName, value, testValue.getUnit(), range[0], range[1]),
                            getRecommendationForParam(param, value, range),
                            "BLOOD_TEST",
                            bloodTest.getId()
                        );
                        alerts.add(alertRepository.save(alert));
                        log.warn("ALERT generated for patient {} - {} = {}", 
                            bloodTest.getPatientId(), testName, value);
                    }
                }
            });
        });

        // Alerte globale si résultat marqué anormal
        if (Boolean.TRUE.equals(bloodTest.getAbnormal()) && alerts.isEmpty()) {
            ClinicalAlert alert = buildAlert(
                bloodTest.getPatientId(), bloodTest.getPatientName(), bloodTest.getMedecinId(),
                AlertType.ABNORMAL_RESULT, AlertSeverity.MEDIUM,
                "Résultats anormaux détectés",
                "Le bilan sanguin contient des valeurs anormales nécessitant une attention médicale.",
                "Réviser les résultats et ajuster le traitement si nécessaire.",
                "BLOOD_TEST", bloodTest.getId()
            );
            alerts.add(alertRepository.save(alert));
        }

        return alerts;
    }

    /**
     * Analyse un calcul GFR et génère une alerte si stade critique ou progression.
     */
    public List<ClinicalAlert> analyzeGFR(GFRCalculation gfr, GFRCalculation previousGFR) {
        List<ClinicalAlert> alerts = new ArrayList<>();

        // Alerte stade 4 ou 5
        if (gfr.getCkdStage() == CKDStage.STAGE_5) {
            alerts.add(alertRepository.save(buildAlert(
                gfr.getPatientId(), gfr.getPatientName(), gfr.getMedecinId(),
                AlertType.CRITICAL_VALUE, AlertSeverity.CRITICAL,
                "URGENT: Insuffisance rénale terminale (Stade 5)",
                String.format("GFR = %.2f mL/min/1.73m² - Stade 5 CKD détecté.", gfr.getGfrValue()),
                "Consultation néphrologue urgente. Évaluation pour dialyse ou transplantation.",
                "GFR", gfr.getId()
            )));
        } else if (gfr.getCkdStage() == CKDStage.STAGE_4) {
            alerts.add(alertRepository.save(buildAlert(
                gfr.getPatientId(), gfr.getPatientName(), gfr.getMedecinId(),
                AlertType.CRITICAL_VALUE, AlertSeverity.HIGH,
                "Insuffisance rénale sévère (Stade 4)",
                String.format("GFR = %.2f mL/min/1.73m² - Stade 4 CKD.", gfr.getGfrValue()),
                "Suivi rapproché. Préparer le patient à la dialyse si progression.",
                "GFR", gfr.getId()
            )));
        }

        // Alerte progression de stade
        if (previousGFR != null && previousGFR.getCkdStage() != gfr.getCkdStage()) {
            if (gfr.getCkdStage().ordinal() > previousGFR.getCkdStage().ordinal()) {
                alerts.add(alertRepository.save(buildAlert(
                    gfr.getPatientId(), gfr.getPatientName(), gfr.getMedecinId(),
                    AlertType.STAGE_PROGRESSION, AlertSeverity.HIGH,
                    "Progression du stade CKD",
                    String.format("Passage de %s à %s (GFR: %.2f → %.2f)",
                        previousGFR.getCkdStage().getName(), gfr.getCkdStage().getName(),
                        previousGFR.getGfrValue(), gfr.getGfrValue()),
                    "Réévaluer le plan de traitement. Intensifier le suivi.",
                    "GFR", gfr.getId()
                )));
            }
        }

        // Alerte déclin rapide (> 25% en une mesure)
        if (previousGFR != null) {
            double declinePercent = ((previousGFR.getGfrValue() - gfr.getGfrValue()) 
                                    / previousGFR.getGfrValue()) * 100;
            if (declinePercent > 25) {
                alerts.add(alertRepository.save(buildAlert(
                    gfr.getPatientId(), gfr.getPatientName(), gfr.getMedecinId(),
                    AlertType.GFR_DETERIORATION, AlertSeverity.CRITICAL,
                    "Déclin rapide du GFR",
                    String.format("GFR a diminué de %.1f%% (%.2f → %.2f mL/min/1.73m²)",
                        declinePercent, previousGFR.getGfrValue(), gfr.getGfrValue()),
                    "Hospitalisation à envisager. Rechercher cause aiguë (déshydratation, infection, néphrotoxiques).",
                    "GFR", gfr.getId()
                )));
            }
        }

        return alerts;
    }

    /**
     * Analyse un résultat de labo et génère des alertes si nécessaire.
     */
    public List<ClinicalAlert> analyzeLabResult(LabResult labResult) {
        List<ClinicalAlert> alerts = new ArrayList<>();

        if (Boolean.TRUE.equals(labResult.getAbnormal())) {
            AlertSeverity severity = AlertSeverity.MEDIUM;
            // Si findings contient des mots critiques, élever la sévérité
            if (labResult.getFindings() != null) {
                String findings = labResult.getFindings().toLowerCase();
                if (findings.contains("critical") || findings.contains("urgent") || findings.contains("critique")) {
                    severity = AlertSeverity.CRITICAL;
                } else if (findings.contains("severe") || findings.contains("sévère")) {
                    severity = AlertSeverity.HIGH;
                }
            }

            alerts.add(alertRepository.save(buildAlert(
                labResult.getPatientId(), labResult.getPatientName(), labResult.getMedecinId(),
                AlertType.ABNORMAL_RESULT, severity,
                "Résultat de laboratoire anormal: " + labResult.getTestName(),
                labResult.getDetails() != null ? labResult.getDetails()
                    : "Résultat anormal détecté pour " + labResult.getTestName(),
                "Réviser les résultats et adapter la prise en charge.",
                "LAB_RESULT", labResult.getId()
            )));
        }

        return alerts;
    }

    public List<ClinicalAlert> getPatientAlerts(String patientId) {
        return alertRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

    public List<ClinicalAlert> getUnacknowledgedAlertsForDoctor(String medecinId) {
        return alertRepository.findByMedecinIdAndAcknowledgedFalseOrderByCreatedAtDesc(medecinId);
    }

    public ClinicalAlert acknowledgeAlert(String alertId, String medecinId) {
        ClinicalAlert alert = alertRepository.findById(alertId)
            .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));
        alert.setAcknowledged(true);
        alert.setAcknowledgedAt(LocalDateTime.now());
        alert.setAcknowledgedBy(medecinId);
        return alertRepository.save(alert);
    }

    // ===== Helpers =====

    private ClinicalAlert buildAlert(String patientId, String patientName, String medecinId,
                                      AlertType type, AlertSeverity severity,
                                      String title, String message, String recommendation,
                                      String sourceType, String sourceId) {
        ClinicalAlert alert = new ClinicalAlert();
        alert.setPatientId(patientId);
        alert.setPatientName(patientName);
        alert.setMedecinId(medecinId);
        alert.setAlertType(type);
        alert.setSeverity(severity);
        alert.setTitle(title);
        alert.setMessage(message);
        alert.setRecommendation(recommendation);
        alert.setSourceType(sourceType);
        alert.setSourceId(sourceId);
        alert.setAcknowledged(false);
        alert.setCreatedAt(LocalDateTime.now());
        return alert;
    }

    private AlertSeverity determineSeverity(String param, double value, double[] range) {
        double deviation = Math.max(range[0] - value, value - range[1]);
        double rangeSize = range[1] - range[0];
        double deviationPercent = (deviation / rangeSize) * 100;

        if (param.equals("potassium") && (value < 2.5 || value > 7.0)) return AlertSeverity.CRITICAL;
        if (param.equals("hemoglobin") && value < 6.0) return AlertSeverity.CRITICAL;
        if (deviationPercent > 50) return AlertSeverity.CRITICAL;
        if (deviationPercent > 25) return AlertSeverity.HIGH;
        return AlertSeverity.MEDIUM;
    }

    private String getRecommendationForParam(String param, double value, double[] range) {
        return switch (param) {
            case "creatinine" -> "Évaluer la fonction rénale. Calculer le GFR. Ajuster les médicaments néphrotoxiques.";
            case "potassium" -> value > range[1]
                ? "Hyperkaliémie: ECG urgent, traitement médical immédiat (kayexalate, insuline-glucose)."
                : "Hypokaliémie: Supplémentation en potassium. Surveiller l'ECG.";
            case "hemoglobin" -> "Anémie sévère: Évaluer la cause. Envisager EPO ou transfusion si < 7 g/dL.";
            case "sodium" -> value < range[0]
                ? "Hyponatrémie: Restriction hydrique. Correction progressive."
                : "Hypernatrémie: Réhydratation progressive. Surveiller neurologie.";
            case "phosphorus" -> "Hyperphosphatémie: Chélateurs du phosphore. Restriction alimentaire.";
            default -> "Consulter le néphrologue pédiatrique pour évaluation.";
        };
    }
}
