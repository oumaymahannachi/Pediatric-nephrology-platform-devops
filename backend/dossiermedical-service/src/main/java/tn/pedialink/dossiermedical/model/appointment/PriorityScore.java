package tn.pedialink.dossiermedical.model.appointment;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PriorityScore {
    private String appointmentId;
    private String patientId;
    private PriorityLevel priorityLevel;
    private Integer totalScore;
    private LocalDateTime calculatedAt;
    
    // Facteurs de calcul
    private Integer medicalUrgencyScore = 0;
    private Integer waitingTimeScore = 0;
    private Integer ageFactorScore = 0;
    private Integer chronicConditionScore = 0;
    private Integer previousCancellationScore = 0;
    
    private String reasoning; // Explication du score
}
