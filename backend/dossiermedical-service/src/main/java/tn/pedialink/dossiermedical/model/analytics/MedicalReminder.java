package tn.pedialink.dossiermedical.model.analytics;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "medical_reminders")
public class MedicalReminder {
    @Id
    private String id;

    private String patientId;
    private String patientName;
    private String medecinId;

    private ReminderType type;
    private ReminderStatus status;
    private String title;
    private String message;

    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime acknowledgedAt;

    private int daysSinceLastConsultation;  // Pour les alertes d'inactivité
    private boolean chronic;                // Patient chronique = suivi obligatoire

    public enum ReminderType {
        APPOINTMENT_REMINDER,       // Rappel RDV dans 24-48h
        MANDATORY_FOLLOWUP,         // Suivi obligatoire (patient chronique)
        INACTIVITY_ALERT,           // Patient absent depuis trop longtemps
        EXAM_FOLLOWUP,              // Résultats d'examen à revoir
        DIALYSIS_SESSION            // Prochaine séance dialyse
    }

    public enum ReminderStatus {
        PENDING, SENT, ACKNOWLEDGED, EXPIRED
    }
}
