package tn.pedialink.dossiermedical.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.pedialink.dossiermedical.model.analytics.DialysisRiskScore;
import tn.pedialink.dossiermedical.model.analytics.DialysisRiskScore.RiskLevel;
import tn.pedialink.dossiermedical.model.dialyse.DialysisSession;
import tn.pedialink.dossiermedical.model.dialyse.StatutSession;
import tn.pedialink.dossiermedical.model.examen.BloodTest;
import tn.pedialink.dossiermedical.model.kidney.CKDStage;
import tn.pedialink.dossiermedical.model.kidney.GFRCalculation;
import tn.pedialink.dossiermedical.repository.BloodTestRepository;
import tn.pedialink.dossiermedical.repository.DialysisSessionRepository;
import tn.pedialink.dossiermedical.repository.GFRCalculationRepository;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service de calcul du score de risque de dialyse pédiatrique.
 * Évalue le risque d'aggravation basé sur GFR, bilans sanguins et historique dialyse.
 */
@Service
@RequiredArgsConstructor
public class DialysisRiskService {

    private final GFRCalculationRepository gfrRepository;
    private final BloodTestRepository bloodTestRepository;
    private final DialysisSessionRepository dialysisSessionRepository;

    /**
     * Calcule le score de risque de dialyse pour un patient.
     * Score sur 100 points basé sur 5 critères cliniques.
     */
    public DialysisRiskScore calculateRisk(String patientId) {
        DialysisRiskScore score = new DialysisRiskScore();
        score.setPatientId(patientId);
        score.setCalculatedAt(LocalDateTime.now());

        Map<String, Integer> breakdown = new LinkedHashMap<>();
        List<String> riskFactors = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        // === Critère 1: Stade CKD (0-35 points) ===
        int gfrScore = evaluateGFRScore(patientId, breakdown, riskFactors, recommendations);

        // === Critère 2: Valeurs biologiques critiques (0-25 points) ===
        int bioScore = evaluateBiologicalScore(patientId, breakdown, riskFactors, recommendations);

        // === Critère 3: Historique dialyse (0-20 points) ===
        int dialysisScore = evaluateDialysisHistory(patientId, breakdown, riskFactors, recommendations);

        // === Critère 4: Tendance GFR (0-15 points) ===
        int trendScore = evaluateGFRTrend(patientId, breakdown, riskFactors, recommendations);

        // === Critère 5: Complications récentes (0-5 points) ===
        int complicationScore = evaluateComplications(patientId, breakdown, riskFactors, recommendations);

        int total = gfrScore + bioScore + dialysisScore + trendScore + complicationScore;
        score.setTotalScore(Math.min(total, 100));
        score.setRiskLevel(RiskLevel.fromScore(score.getTotalScore()));
        score.setScoreBreakdown(breakdown);
        score.setRiskFactors(riskFactors);
        score.setRecommendations(recommendations);

        // Recommandation globale selon niveau de risque
        addGlobalRecommendation(score.getRiskLevel(), recommendations);

        return score;
    }

    // ===== Critère 1: GFR / Stade CKD =====
    private int evaluateGFRScore(String patientId, Map<String, Integer> breakdown,
                                  List<String> factors, List<String> recs) {
        List<GFRCalculation> history = gfrRepository.findByPatientIdOrderByCalculationDateDesc(patientId);
        if (history.isEmpty()) {
            breakdown.put("GFR/Stade CKD", 0);
            return 0;
        }

        GFRCalculation latest = history.get(0);
        int points = switch (latest.getCkdStage()) {
            case STAGE_1 -> 0;
            case STAGE_2 -> 5;
            case STAGE_3A -> 10;
            case STAGE_3B -> 18;
            case STAGE_4 -> 28;
            case STAGE_5 -> 35;
        };

        breakdown.put("GFR/Stade CKD (" + latest.getCkdStage().getName() + ")", points);

        if (latest.getCkdStage().ordinal() >= CKDStage.STAGE_4.ordinal()) {
            factors.add("Stade CKD avancé: " + latest.getCkdStage().getName());
            recs.add("Préparer le patient et la famille à la dialyse ou transplantation.");
        }
        return points;
    }

    // ===== Critère 2: Valeurs biologiques =====
    private int evaluateBiologicalScore(String patientId, Map<String, Integer> breakdown,
                                         List<String> factors, List<String> recs) {
        List<BloodTest> tests = bloodTestRepository.findByPatientId(patientId);
        if (tests.isEmpty()) {
            breakdown.put("Valeurs biologiques", 0);
            return 0;
        }

        BloodTest latest = tests.stream()
            .max(Comparator.comparing(BloodTest::getTestDate))
            .orElse(tests.get(0));

        int points = 0;
        if (Boolean.TRUE.equals(latest.getAbnormal())) {
            points += 10;
            factors.add("Résultats biologiques anormaux");
        }

        // Vérifier créatinine élevée
        if (latest.getResults() != null) {
            latest.getResults().forEach((key, val) -> {
                if (key.toLowerCase().contains("creatinine") && val.getValue() != null) {
                    if (val.getValue() > 2.0) factors.add("Créatinine élevée: " + val.getValue() + " mg/dL");
                }
                if (key.toLowerCase().contains("potassium") && val.getValue() != null) {
                    if (val.getValue() > 5.5) factors.add("Hyperkaliémie: " + val.getValue() + " mEq/L");
                }
            });
            if (!factors.isEmpty()) points = Math.min(points + 15, 25);
        }

        breakdown.put("Valeurs biologiques", points);
        if (points > 10) recs.add("Corriger les déséquilibres électrolytiques. Adapter le régime alimentaire.");
        return points;
    }

    // ===== Critère 3: Historique dialyse =====
    private int evaluateDialysisHistory(String patientId, Map<String, Integer> breakdown,
                                         List<String> factors, List<String> recs) {
        List<DialysisSession> sessions = dialysisSessionRepository.findByPatientId(patientId);
        if (sessions.isEmpty()) {
            breakdown.put("Historique dialyse", 0);
            return 0;
        }

        int points = 0;
        long completedSessions = sessions.stream()
            .filter(s -> s.getStatus() == StatutSession.COMPLETED).count();
        long complications = sessions.stream()
            .filter(s -> s.getComplications() != null && !s.getComplications().isEmpty()).count();

        if (completedSessions > 0) {
            points += 10;
            factors.add("Patient déjà sous dialyse (" + completedSessions + " séances)");
        }
        if (complications > 0) {
            points += 10;
            factors.add("Complications dialyse antérieures (" + complications + " épisodes)");
            recs.add("Surveiller les complications lors des prochaines séances.");
        }

        breakdown.put("Historique dialyse", Math.min(points, 20));
        return Math.min(points, 20);
    }

    // ===== Critère 4: Tendance GFR =====
    private int evaluateGFRTrend(String patientId, Map<String, Integer> breakdown,
                                  List<String> factors, List<String> recs) {
        List<GFRCalculation> history = gfrRepository.findByPatientIdOrderByCalculationDateDesc(patientId);
        if (history.size() < 2) {
            breakdown.put("Tendance GFR", 0);
            return 0;
        }

        double current = history.get(0).getGfrValue();
        double previous = history.get(1).getGfrValue();
        double changePercent = ((previous - current) / previous) * 100;

        int points = 0;
        if (changePercent > 25) {
            points = 15;
            factors.add(String.format("Déclin rapide du GFR: -%.1f%%", changePercent));
            recs.add("Déclin rapide détecté. Bilan étiologique urgent.");
        } else if (changePercent > 10) {
            points = 8;
            factors.add(String.format("Déclin modéré du GFR: -%.1f%%", changePercent));
        } else if (changePercent > 0) {
            points = 3;
        }

        breakdown.put("Tendance GFR", points);
        return points;
    }

    // ===== Critère 5: Complications récentes =====
    private int evaluateComplications(String patientId, Map<String, Integer> breakdown,
                                       List<String> factors, List<String> recs) {
        List<DialysisSession> recentSessions = dialysisSessionRepository.findByPatientId(patientId);
        long recentComplications = recentSessions.stream()
            .filter(s -> s.getComplications() != null && !s.getComplications().isEmpty())
            .filter(s -> s.getUpdatedAt() != null && 
                         s.getUpdatedAt().isAfter(LocalDateTime.now().minusMonths(1)))
            .count();

        int points = recentComplications > 0 ? 5 : 0;
        if (points > 0) {
            factors.add("Complications récentes lors des séances de dialyse");
            recs.add("Revoir le protocole de dialyse.");
        }

        breakdown.put("Complications récentes", points);
        return points;
    }

    private void addGlobalRecommendation(RiskLevel level, List<String> recs) {
        recs.add("--- " + level.getDescription() + " ---");
        switch (level) {
            case LOW -> recs.add("Suivi standard tous les 3 mois.");
            case MODERATE -> recs.add("Suivi mensuel. Optimiser le traitement conservateur.");
            case HIGH -> recs.add("Consultation néphrologue dans les 2 semaines. Préparer accès vasculaire.");
            case CRITICAL -> recs.add("URGENT: Hospitalisation à envisager. Initiation dialyse probable.");
        }
    }
}
