package tn.pedialink.dossiermedical.model.analytics;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "clinical_alerts")
public class ClinicalAlert {

    @Id
    private String id;

    private String patientId;
    private String patientName;
    private String medecinId;

    private AlertType alertType;
    private AlertSeverity severity;
    private String title;
    private String message;
    private String recommendation;

    private String sourceType;  // BLOOD_TEST, GFR, DIALYSIS, LAB_RESULT
    private String sourceId;    // ID de l'examen source

    private boolean acknowledged;
    private LocalDateTime acknowledgedAt;
    private String acknowledgedBy;

    private LocalDateTime createdAt;

    public enum AlertType {
        CRITICAL_VALUE,         // Valeur critique (ex: créatinine très élevée)
        GFR_DETERIORATION,      // Détérioration rapide du GFR
        ABNORMAL_RESULT,        // Résultat anormal
        DIALYSIS_COMPLICATION,  // Complication dialyse
        FOLLOW_UP_REQUIRED,     // Suivi requis
        STAGE_PROGRESSION       // Progression stade CKD
    }

    public enum AlertSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}
