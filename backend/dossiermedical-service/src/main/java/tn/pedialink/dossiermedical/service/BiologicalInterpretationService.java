package tn.pedialink.dossiermedical.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.pedialink.dossiermedical.model.analytics.BiologicalInterpretation;
import tn.pedialink.dossiermedical.model.analytics.BiologicalInterpretation.ParameterAnalysis;
import tn.pedialink.dossiermedical.model.examen.BloodTest;
import tn.pedialink.dossiermedical.repository.BloodTestRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Service d'interprétation automatique des résultats biologiques pédiatriques.
 * Utilise les normes pédiatriques par tranche d'âge (en mois).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BiologicalInterpretationService {

    private final BloodTestRepository bloodTestRepository;

    // Normes pédiatriques créatinine (mg/dL) par âge en mois
    // Format: {ageMinMonths, ageMaxMonths, normalMin, normalMax, criticalMax}
    private static final double[][] CREATININE_NORMS = {
        {0,   3,   0.2, 0.5, 1.0},
        {3,   12,  0.2, 0.4, 0.8},
        {12,  24,  0.2, 0.5, 1.0},
        {24,  60,  0.3, 0.6, 1.2},
        {60,  120, 0.4, 0.8, 1.5},
        {120, 216, 0.5, 1.0, 2.0}
    };

    // Normes urée (mg/dL)
    private static final double[][] UREA_NORMS = {
        {0,   12,  5,  15, 30},
        {12,  60,  5,  18, 40},
        {60,  216, 7,  20, 50}
    };

    // Normes potassium (mEq/L)
    private static final double[][] POTASSIUM_NORMS = {
        {0,   12,  3.5, 6.0, 7.0},
        {12,  216, 3.5, 5.5, 6.5}
    };

    // Normes sodium (mEq/L) - identiques tous âges
    private static final double[] SODIUM_NORMS = {135, 145, 125, 155};

    // Normes hémoglobine (g/dL)
    private static final double[][] HEMOGLOBIN_NORMS = {
        {0,   3,   13.5, 20.0, 7.0},
        {3,   12,  9.5,  13.5, 6.0},
        {12,  60,  10.5, 14.0, 6.5},
        {60,  120, 11.5, 15.5, 7.0},
        {120, 216, 12.0, 16.0, 7.0}
    };

    /**
     * Interprète les résultats biologiques d'un patient selon son âge et poids.
     */
    public BiologicalInterpretation interpret(String patientId, int ageMonths, double weightKg) {
        List<BloodTest> tests = bloodTestRepository.findByPatientId(patientId);

        BiologicalInterpretation result = new BiologicalInterpretation();
        result.setPatientId(patientId);
        result.setPatientAgeMonths(ageMonths);
        result.setPatientWeightKg(weightKg);
        result.setAnalysisDate(LocalDateTime.now());
        result.setAnalyses(new ArrayList<>());
        result.setCriticalAlerts(new ArrayList<>());
        result.setRecommendations(new ArrayList<>());

        if (tests.isEmpty()) {
            result.getCriticalAlerts().add("Aucun bilan biologique disponible pour ce patient.");
            return result;
        }

        // Prendre le bilan le plus récent
        BloodTest latest = tests.stream()
            .max(Comparator.comparing(BloodTest::getTestDate))
            .orElse(tests.get(0));

        log.info("Latest blood test: id={}, type={}, results={}", 
            latest.getId(), latest.getTestType(), 
            latest.getResults() != null ? latest.getResults().keySet() : "NULL");

        if (latest.getResults() == null || latest.getResults().isEmpty()) {
            result.getCriticalAlerts().add("Le bilan biologique ne contient pas de valeurs détaillées (créatinine, urée, etc.).");
            result.getRecommendations().add("Veuillez éditer le bilan et ajouter les valeurs biologiques (créatinine, urée, potassium, sodium, hémoglobine).");
            return result;
        }

        Double creatinine = null, urea = null, potassium = null, sodium = null, hemoglobin = null;

        // Analyser chaque paramètre
        for (Map.Entry<String, BloodTest.TestValue> entry : latest.getResults().entrySet()) {
            String key = entry.getKey().toLowerCase();
            BloodTest.TestValue val = entry.getValue();
            if (val.getValue() == null) continue;

            double value = val.getValue();

            if (key.contains("creatinine") || key.contains("créatinine")) {
                creatinine = value;
                result.getAnalyses().add(analyzeParameter("Créatinine", value, val.getUnit(),
                    CREATININE_NORMS, ageMonths, "mg/dL"));
                checkCreatinineCritical(value, ageMonths, result);
            } else if (key.contains("urea") || key.contains("urée") || key.contains("uree")) {
                urea = value;
                result.getAnalyses().add(analyzeParameter("Urée", value, val.getUnit(),
                    UREA_NORMS, ageMonths, "mg/dL"));
            } else if (key.contains("potassium")) {
                potassium = value;
                result.getAnalyses().add(analyzeParameterFixed("Potassium", value, val.getUnit(),
                    getPotassiumNorm(ageMonths)));
                checkPotassiumCritical(value, result);
            } else if (key.contains("sodium")) {
                sodium = value;
                result.getAnalyses().add(analyzeParameterFixed("Sodium", value, val.getUnit(),
                    SODIUM_NORMS));
                checkSodiumCritical(value, result);
            } else if (key.contains("hemoglobin") || key.contains("hémoglobine")) {
                hemoglobin = value;
                result.getAnalyses().add(analyzeParameter("Hémoglobine", value, val.getUnit(),
                    HEMOGLOBIN_NORMS, ageMonths, "g/dL"));
                checkAnemia(value, ageMonths, result);
            }
        }

        // Calcul ratio créatinine/urée
        if (creatinine != null && urea != null && creatinine > 0) {
            double ratio = urea / creatinine;
            result.setCreatinineUrineRatio(Math.round(ratio * 100.0) / 100.0);
            interpretCreatinineUreaRatio(ratio, result);
        }

        // Détection IRA (créatinine doublée en 48h)
        detectAcuteKidneyInjury(patientId, tests, result);

        // Recommandations finales
        generateRecommendations(result);

        return result;
    }

    // ===== Analyse par paramètre =====

    private ParameterAnalysis analyzeParameter(String name, double value, String unit,
                                                double[][] norms, int ageMonths, String defaultUnit) {
        ParameterAnalysis analysis = new ParameterAnalysis();
        analysis.setParameterName(name);
        analysis.setValue(value);
        analysis.setUnit(unit != null ? unit : defaultUnit);

        for (double[] norm : norms) {
            if (ageMonths >= norm[0] && ageMonths < norm[1]) {
                analysis.setNormalMin(norm[2]);
                analysis.setNormalMax(norm[3]);
                analysis.setAgeGroup((int)norm[0] + "-" + (int)norm[1] + " mois");

                double criticalMax = norm.length > 4 ? norm[4] : norm[3] * 2;
                if (value < norm[2] * 0.7) {
                    analysis.setStatus("CRITICAL_LOW");
                    analysis.setInterpretation(name + " très bas - valeur critique");
                } else if (value < norm[2]) {
                    analysis.setStatus("LOW");
                    analysis.setInterpretation(name + " en dessous de la normale");
                } else if (value > criticalMax) {
                    analysis.setStatus("CRITICAL_HIGH");
                    analysis.setInterpretation(name + " très élevé - valeur critique");
                } else if (value > norm[3]) {
                    analysis.setStatus("HIGH");
                    analysis.setInterpretation(name + " au-dessus de la normale");
                } else {
                    analysis.setStatus("NORMAL");
                    analysis.setInterpretation(name + " dans les normes pédiatriques");
                }
                return analysis;
            }
        }

        analysis.setStatus("UNKNOWN");
        analysis.setInterpretation("Normes non disponibles pour cet âge");
        return analysis;
    }

    private ParameterAnalysis analyzeParameterFixed(String name, double value, String unit, double[] norm) {
        ParameterAnalysis analysis = new ParameterAnalysis();
        analysis.setParameterName(name);
        analysis.setValue(value);
        analysis.setUnit(unit != null ? unit : "mEq/L");
        analysis.setNormalMin(norm[0]);
        analysis.setNormalMax(norm[1]);

        if (value < norm[2]) {
            analysis.setStatus("CRITICAL_LOW");
            analysis.setInterpretation(name + " critique - valeur dangereusement basse");
        } else if (value < norm[0]) {
            analysis.setStatus("LOW");
            analysis.setInterpretation(name + " en dessous de la normale");
        } else if (value > norm[3]) {
            analysis.setStatus("CRITICAL_HIGH");
            analysis.setInterpretation(name + " critique - valeur dangereusement élevée");
        } else if (value > norm[1]) {
            analysis.setStatus("HIGH");
            analysis.setInterpretation(name + " au-dessus de la normale");
        } else {
            analysis.setStatus("NORMAL");
            analysis.setInterpretation(name + " dans les normes");
        }
        return analysis;
    }

    // ===== Détections critiques =====

    private void checkCreatinineCritical(double value, int ageMonths, BiologicalInterpretation result) {
        double criticalThreshold = ageMonths < 12 ? 0.8 : ageMonths < 60 ? 1.2 : 2.0;
        if (value > criticalThreshold) {
            result.getCriticalAlerts().add(
                String.format("CRITIQUE: Créatinine = %.2f mg/dL (seuil critique: %.1f) - Insuffisance rénale probable", value, criticalThreshold));
        }
    }

    private void checkPotassiumCritical(double value, BiologicalInterpretation result) {
        if (value > 6.5) {
            result.getCriticalAlerts().add(
                String.format("URGENCE: Hyperkaliémie sévère K+ = %.1f mEq/L - Risque cardiaque immédiat", value));
        } else if (value < 2.5) {
            result.getCriticalAlerts().add(
                String.format("URGENCE: Hypokaliémie sévère K+ = %.1f mEq/L - Risque arythmie", value));
        }
    }

    private void checkSodiumCritical(double value, BiologicalInterpretation result) {
        if (value < 125) {
            result.getCriticalAlerts().add(
                String.format("URGENCE: Hyponatrémie sévère Na+ = %.1f mEq/L - Risque neurologique", value));
        } else if (value > 155) {
            result.getCriticalAlerts().add(
                String.format("URGENCE: Hypernatrémie sévère Na+ = %.1f mEq/L", value));
        }
    }

    private void checkAnemia(double value, int ageMonths, BiologicalInterpretation result) {
        double severeThreshold = ageMonths < 12 ? 7.0 : 7.5;
        if (value < severeThreshold) {
            result.getCriticalAlerts().add(
                String.format("Anémie sévère: Hb = %.1f g/dL - Transfusion à envisager", value));
        }
    }

    private void interpretCreatinineUreaRatio(double ratio, BiologicalInterpretation result) {
        // Ratio normal: 10-20
        if (ratio > 40) {
            result.getCriticalAlerts().add(
                String.format("Ratio Urée/Créatinine élevé (%.1f) - Suspicion déshydratation ou insuffisance prérénale", ratio));
        } else if (ratio < 5) {
            result.getCriticalAlerts().add(
                String.format("Ratio Urée/Créatinine bas (%.1f) - Suspicion atteinte rénale intrinsèque", ratio));
        }
    }

    /**
     * Détecte une IRA si la créatinine a doublé en moins de 48h.
     */
    private void detectAcuteKidneyInjury(String patientId, List<BloodTest> tests,
                                          BiologicalInterpretation result) {
        if (tests.size() < 2) return;

        // Trier par date décroissante
        tests.sort((a, b) -> b.getTestDate().compareTo(a.getTestDate()));

        BloodTest latest = tests.get(0);
        Double latestCreatinine = getCreatinineValue(latest);
        if (latestCreatinine == null) return;

        // Chercher un bilan dans les 48h précédentes
        for (int i = 1; i < tests.size(); i++) {
            BloodTest previous = tests.get(i);
            long hoursDiff = ChronoUnit.HOURS.between(previous.getTestDate(), latest.getTestDate());

            if (hoursDiff <= 48) {
                Double prevCreatinine = getCreatinineValue(previous);
                if (prevCreatinine != null && prevCreatinine > 0) {
                    double ratio = latestCreatinine / prevCreatinine;

                    if (ratio >= 3.0) {
                        result.setAcuteKidneyInjuryDetected(true);
                        result.setAkiSeverity("STAGE_3");
                        result.getCriticalAlerts().add(
                            String.format("IRA STADE 3: Créatinine x%.1f en %dh (%.2f → %.2f mg/dL) - HOSPITALISATION URGENTE",
                                ratio, hoursDiff, prevCreatinine, latestCreatinine));
                    } else if (ratio >= 2.0) {
                        result.setAcuteKidneyInjuryDetected(true);
                        result.setAkiSeverity("STAGE_2");
                        result.getCriticalAlerts().add(
                            String.format("IRA STADE 2: Créatinine x%.1f en %dh (%.2f → %.2f mg/dL) - Consultation urgente",
                                ratio, hoursDiff, prevCreatinine, latestCreatinine));
                    } else if (ratio >= 1.5) {
                        result.setAcuteKidneyInjuryDetected(true);
                        result.setAkiSeverity("STAGE_1");
                        result.getCriticalAlerts().add(
                            String.format("IRA STADE 1: Créatinine x%.1f en %dh (%.2f → %.2f mg/dL) - Surveillance rapprochée",
                                ratio, hoursDiff, prevCreatinine, latestCreatinine));
                    }
                    break;
                }
            }
        }
    }

    private Double getCreatinineValue(BloodTest test) {
        if (test.getResults() == null) return null;
        return test.getResults().entrySet().stream()
            .filter(e -> e.getKey().toLowerCase().contains("creatinine") ||
                         e.getKey().toLowerCase().contains("créatinine"))
            .map(e -> e.getValue().getValue())
            .filter(Objects::nonNull)
            .findFirst().orElse(null);
    }

    private double[] getPotassiumNorm(int ageMonths) {
        if (ageMonths < 12) return new double[]{3.5, 6.0, 2.5, 7.0};
        return new double[]{3.5, 5.5, 2.5, 6.5};
    }

    private void generateRecommendations(BiologicalInterpretation result) {
        if (result.isAcuteKidneyInjuryDetected()) {
            result.getRecommendations().add("Arrêter les médicaments néphrotoxiques (AINS, aminosides).");
            result.getRecommendations().add("Bilan hydrique strict. Évaluer la volémie.");
            result.getRecommendations().add("Contrôle créatinine dans 24h.");
        }
        if (!result.getCriticalAlerts().isEmpty()) {
            result.getRecommendations().add("Consultation néphrologue pédiatrique urgente.");
        }
        if (result.getCreatinineUrineRatio() != null && result.getCreatinineUrineRatio() > 40) {
            result.getRecommendations().add("Réhydratation IV à envisager. Évaluer les pertes hydriques.");
        }
    }
}
