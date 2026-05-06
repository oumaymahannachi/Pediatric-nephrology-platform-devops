package tn.pedialink.dossiermedical.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.pedialink.dossiermedical.model.analytics.PathologyEvolution;
import tn.pedialink.dossiermedical.model.analytics.PathologyEvolution.ConsultationSnapshot;
import tn.pedialink.dossiermedical.model.analytics.PathologyEvolution.EvolutionTrend;
import tn.pedialink.dossiermedical.model.consultation.Consultation;
import tn.pedialink.dossiermedical.model.examen.BloodTest;
import tn.pedialink.dossiermedical.model.kidney.GFRCalculation;
import tn.pedialink.dossiermedical.repository.BloodTestRepository;
import tn.pedialink.dossiermedical.repository.ConsultationRepository;
import tn.pedialink.dossiermedical.repository.GFRCalculationRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de suivi d'évolution de pathologie rénale pédiatrique.
 * Compare les consultations successives et détecte progression/régression.
 */
@Service
@RequiredArgsConstructor
public class PathologyEvolutionService {

    private final ConsultationRepository consultationRepository;
    private final GFRCalculationRepository gfrRepository;
    private final BloodTestRepository bloodTestRepository;

    /**
     * Analyse l'évolution complète de la pathologie d'un patient.
     */
    public PathologyEvolution analyzeEvolution(String patientId) {
        PathologyEvolution evolution = new PathologyEvolution();
        evolution.setPatientId(patientId);
        evolution.setAnalysisDate(LocalDateTime.now());
        evolution.setRecommendations(new ArrayList<>());

        // Récupérer l'historique
        List<GFRCalculation> gfrHistory = gfrRepository
            .findByPatientIdOrderByCalculationDateDesc(patientId);
        List<BloodTest> bloodTests = bloodTestRepository.findByPatientId(patientId);
        List<Consultation> consultations = consultationRepository.findByPatientId(patientId);

        // Construire les snapshots de consultations
        List<ConsultationSnapshot> snapshots = buildSnapshots(consultations, gfrHistory, bloodTests);
        evolution.setConsultationHistory(snapshots);

        // Analyser les tendances
        evolution.setGfrTrend(analyzeGFRTrend(gfrHistory));
        evolution.setCreatinineTrend(analyzeCreatinineTrend(bloodTests));
        evolution.setUreaTrend(analyzeUreaTrend(bloodTests));

        // Progression globale
        String overall = determineOverallProgression(evolution);
        evolution.setOverallProgression(overall);
        evolution.setClinicalSummary(buildClinicalSummary(evolution, gfrHistory));

        // Recommandations
        generateRecommendations(evolution);

        return evolution;
    }

    private List<ConsultationSnapshot> buildSnapshots(List<Consultation> consultations,
                                                       List<GFRCalculation> gfrHistory,
                                                       List<BloodTest> bloodTests) {
        return consultations.stream()
            .sorted(Comparator.comparing(Consultation::getDateRendezVous))
            .map(c -> {
                ConsultationSnapshot snap = new ConsultationSnapshot();
                snap.setDate(c.getDateRendezVous());
                snap.setDiagnostic(c.getDiagnostic());
                snap.setSymptoms(extractSymptoms(c.getObservationsCliniques()));

                // GFR le plus proche de cette consultation
                gfrHistory.stream()
                    .min(Comparator.comparingLong(g ->
                        Math.abs(g.getCalculationDate().toLocalDate()
                            .toEpochDay() - c.getDateRendezVous().toLocalDate().toEpochDay())))
                    .ifPresent(g -> {
                        snap.setGfrValue(g.getGfrValue());
                        snap.setCkdStage(g.getCkdStage() != null ? g.getCkdStage().getName() : null);
                    });

                // Créatinine la plus proche
                bloodTests.stream()
                    .filter(bt -> bt.getResults() != null)
                    .min(Comparator.comparingLong(bt ->
                        Math.abs(bt.getTestDate().toLocalDate()
                            .toEpochDay() - c.getDateRendezVous().toLocalDate().toEpochDay())))
                    .ifPresent(bt -> {
                        bt.getResults().forEach((key, val) -> {
                            if (key.toLowerCase().contains("creatinine") && val.getValue() != null)
                                snap.setCreatinineValue(val.getValue());
                            if (key.toLowerCase().contains("urea") || key.toLowerCase().contains("urée"))
                                if (val.getValue() != null) snap.setUreaValue(val.getValue());
                        });
                    });

                return snap;
            })
            .collect(Collectors.toList());
    }

    private EvolutionTrend analyzeGFRTrend(List<GFRCalculation> history) {
        if (history.size() < 2) return EvolutionTrend.INSUFFICIENT_DATA;

        List<GFRCalculation> sorted = history.stream()
            .sorted(Comparator.comparing(GFRCalculation::getCalculationDate))
            .collect(Collectors.toList());

        double first = sorted.get(0).getGfrValue();
        double last = sorted.get(sorted.size() - 1).getGfrValue();
        double changePercent = ((last - first) / first) * 100;

        if (changePercent > 10) return EvolutionTrend.IMPROVING;
        if (changePercent < -30) return EvolutionTrend.RAPID_WORSENING;
        if (changePercent < -10) return EvolutionTrend.WORSENING;
        return EvolutionTrend.STABLE;
    }

    private EvolutionTrend analyzeCreatinineTrend(List<BloodTest> tests) {
        List<Double> values = tests.stream()
            .filter(t -> t.getResults() != null)
            .sorted(Comparator.comparing(BloodTest::getTestDate))
            .flatMap(t -> t.getResults().entrySet().stream()
                .filter(e -> e.getKey().toLowerCase().contains("creatinine"))
                .map(e -> e.getValue().getValue())
                .filter(Objects::nonNull))
            .collect(Collectors.toList());

        if (values.size() < 2) return EvolutionTrend.INSUFFICIENT_DATA;

        double first = values.get(0);
        double last = values.get(values.size() - 1);
        double changePercent = ((last - first) / first) * 100;

        // Créatinine: augmentation = aggravation
        if (changePercent > 50) return EvolutionTrend.RAPID_WORSENING;
        if (changePercent > 20) return EvolutionTrend.WORSENING;
        if (changePercent < -20) return EvolutionTrend.IMPROVING;
        return EvolutionTrend.STABLE;
    }

    private EvolutionTrend analyzeUreaTrend(List<BloodTest> tests) {
        List<Double> values = tests.stream()
            .filter(t -> t.getResults() != null)
            .sorted(Comparator.comparing(BloodTest::getTestDate))
            .flatMap(t -> t.getResults().entrySet().stream()
                .filter(e -> e.getKey().toLowerCase().contains("urea") ||
                             e.getKey().toLowerCase().contains("urée"))
                .map(e -> e.getValue().getValue())
                .filter(Objects::nonNull))
            .collect(Collectors.toList());

        if (values.size() < 2) return EvolutionTrend.INSUFFICIENT_DATA;

        double first = values.get(0);
        double last = values.get(values.size() - 1);
        double changePercent = ((last - first) / first) * 100;

        if (changePercent > 50) return EvolutionTrend.RAPID_WORSENING;
        if (changePercent > 20) return EvolutionTrend.WORSENING;
        if (changePercent < -20) return EvolutionTrend.IMPROVING;
        return EvolutionTrend.STABLE;
    }

    private String determineOverallProgression(PathologyEvolution evolution) {
        int worseningScore = 0;

        if (evolution.getGfrTrend() == EvolutionTrend.RAPID_WORSENING) worseningScore += 3;
        else if (evolution.getGfrTrend() == EvolutionTrend.WORSENING) worseningScore += 2;
        else if (evolution.getGfrTrend() == EvolutionTrend.IMPROVING) worseningScore -= 1;

        if (evolution.getCreatinineTrend() == EvolutionTrend.RAPID_WORSENING) worseningScore += 3;
        else if (evolution.getCreatinineTrend() == EvolutionTrend.WORSENING) worseningScore += 2;
        else if (evolution.getCreatinineTrend() == EvolutionTrend.IMPROVING) worseningScore -= 1;

        if (worseningScore >= 5) return "RAPID_WORSENING";
        if (worseningScore >= 3) return "WORSENING";
        if (worseningScore <= -1) return "IMPROVING";
        return "STABLE";
    }

    private String buildClinicalSummary(PathologyEvolution evolution, List<GFRCalculation> gfrHistory) {
        StringBuilder sb = new StringBuilder();

        if (!gfrHistory.isEmpty()) {
            GFRCalculation latest = gfrHistory.get(0);
            sb.append(String.format("Dernier GFR: %.2f mL/min/1.73m² (%s). ",
                latest.getGfrValue(),
                latest.getCkdStage() != null ? latest.getCkdStage().getName() : "stade inconnu"));
        }

        sb.append("Tendance GFR: ").append(translateTrend(evolution.getGfrTrend())).append(". ");
        sb.append("Créatinine: ").append(translateTrend(evolution.getCreatinineTrend())).append(". ");

        switch (evolution.getOverallProgression()) {
            case "RAPID_WORSENING" -> sb.append("AGGRAVATION RAPIDE détectée - intervention urgente requise.");
            case "WORSENING" -> sb.append("Progression de la maladie rénale - réévaluation du traitement nécessaire.");
            case "IMPROVING" -> sb.append("Amélioration de la fonction rénale observée.");
            default -> sb.append("Évolution stable.");
        }

        return sb.toString();
    }

    private String translateTrend(EvolutionTrend trend) {
        return switch (trend) {
            case STABLE -> "stable";
            case IMPROVING -> "en amélioration";
            case WORSENING -> "en dégradation";
            case RAPID_WORSENING -> "en dégradation rapide";
            case INSUFFICIENT_DATA -> "données insuffisantes";
        };
    }

    private List<String> extractSymptoms(String observations) {
        if (observations == null || observations.isEmpty()) return List.of();
        List<String> symptoms = new ArrayList<>();
        String[] keywords = {"oedème", "hypertension", "protéinurie", "hématurie",
                             "fatigue", "nausée", "vomissement", "douleur"};
        String lower = observations.toLowerCase();
        for (String kw : keywords) {
            if (lower.contains(kw)) symptoms.add(kw);
        }
        return symptoms;
    }

    private void generateRecommendations(PathologyEvolution evolution) {
        switch (evolution.getOverallProgression()) {
            case "RAPID_WORSENING" -> {
                evolution.getRecommendations().add("Hospitalisation urgente à envisager.");
                evolution.getRecommendations().add("Réévaluation complète du traitement immunosuppresseur.");
                evolution.getRecommendations().add("Préparer le patient à la dialyse si GFR < 15.");
            }
            case "WORSENING" -> {
                evolution.getRecommendations().add("Consultation néphrologue dans les 2 semaines.");
                evolution.getRecommendations().add("Optimiser le contrôle tensionnel et protéinurie.");
                evolution.getRecommendations().add("Contrôle GFR dans 1 mois.");
            }
            case "IMPROVING" -> {
                evolution.getRecommendations().add("Maintenir le traitement actuel.");
                evolution.getRecommendations().add("Contrôle GFR dans 3 mois.");
            }
            default -> evolution.getRecommendations().add("Suivi standard tous les 3 mois.");
        }
    }
}
