package tn.pedialink.dossiermedical.model.analytics;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class DialysisRiskScore {
    private String patientId;

    private int totalScore;         // Score total sur 100
    private RiskLevel riskLevel;
    private Map<String, Integer> scoreBreakdown; // Détail par critère
    private List<String> riskFactors;            // Facteurs de risque identifiés
    private List<String> recommendations;

    private LocalDateTime calculatedAt;

    public enum RiskLevel {
        LOW(0, 30, "Faible risque - Surveillance standard"),
        MODERATE(31, 60, "Risque modéré - Surveillance renforcée"),
        HIGH(61, 80, "Risque élevé - Consultation urgente recommandée"),
        CRITICAL(81, 100, "Risque critique - Préparation dialyse à envisager");

        private final int minScore;
        private final int maxScore;
        private final String description;

        RiskLevel(int minScore, int maxScore, String description) {
            this.minScore = minScore;
            this.maxScore = maxScore;
            this.description = description;
        }

        public String getDescription() { return description; }

        public static RiskLevel fromScore(int score) {
            for (RiskLevel level : values()) {
                if (score >= level.minScore && score <= level.maxScore) return level;
            }
            return CRITICAL;
        }
    }
}
