package tn.pedialink.dossiermedical.model.analytics;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BiologicalInterpretation {
    private String patientId;
    private String patientName;
    private int patientAgeMonths;
    private double patientWeightKg;

    private List<ParameterAnalysis> analyses;
    private List<String> criticalAlerts;     // IRA, hyperkaliémie sévère, etc.
    private List<String> recommendations;

    private Double creatinineUrineRatio;     // Ratio créatinine/urée
    private boolean acuteKidneyInjuryDetected; // Créatinine doublée en 48h
    private String akiSeverity;              // STAGE_1, STAGE_2, STAGE_3

    private LocalDateTime analysisDate;

    @Data
    public static class ParameterAnalysis {
        private String parameterName;
        private Double value;
        private String unit;
        private Double normalMin;
        private Double normalMax;
        private String status;       // NORMAL, LOW, HIGH, CRITICAL_LOW, CRITICAL_HIGH
        private String interpretation;
        private String ageGroup;     // Norme utilisée (ex: "6-12 mois")
    }
}
