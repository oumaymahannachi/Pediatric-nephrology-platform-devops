package tn.pedialink.dossiermedical.model.analytics;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PathologyEvolution {
    private String patientId;

    private EvolutionTrend gfrTrend;
    private EvolutionTrend creatinineTrend;
    private EvolutionTrend ureaTrend;

    private String overallProgression;  // STABLE, IMPROVING, WORSENING, RAPID_WORSENING
    private String clinicalSummary;
    private List<String> recommendations;

    private List<ConsultationSnapshot> consultationHistory;
    private LocalDateTime analysisDate;

    public enum EvolutionTrend {
        STABLE, IMPROVING, WORSENING, RAPID_WORSENING, INSUFFICIENT_DATA
    }

    @Data
    public static class ConsultationSnapshot {
        private LocalDateTime date;
        private String diagnostic;
        private Double gfrValue;
        private Double creatinineValue;
        private Double ureaValue;
        private String ckdStage;
        private List<String> symptoms;
    }
}
