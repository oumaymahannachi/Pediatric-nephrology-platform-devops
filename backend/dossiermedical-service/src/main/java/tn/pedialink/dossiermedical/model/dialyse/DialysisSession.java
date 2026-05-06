package tn.pedialink.dossiermedical.model.dialyse;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "dialysis_sessions")
public class DialysisSession {
    @Id
    private String id;
    private String prescriptionId;         // Link to prescription
    private String patientId;
    private String patientName;
    private String medecinId;
    private LocalDateTime scheduledDate;   // Scheduled date/time
    private LocalDateTime startTime;       // Actual start time
    private LocalDateTime endTime;         // Actual end time
    private StatutSession status;
    
    // Pre-dialysis vitals
    private Double preWeight;              // kg
    private Double preBloodPressureSystolic;
    private Double preBloodPressureDiastolic;
    private Double prePulse;
    private Double preTemperature;
    
    // Post-dialysis vitals
    private Double postWeight;             // kg
    private Double postBloodPressureSystolic;
    private Double postBloodPressureDiastolic;
    private Double postPulse;
    private Double postTemperature;
    
    // Session details
    private Double ultrafiltrationVolume;  // ml
    private String complications;          // Any complications
    private String notes;                  // Session notes
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
