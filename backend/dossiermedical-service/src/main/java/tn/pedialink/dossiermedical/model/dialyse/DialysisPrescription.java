package tn.pedialink.dossiermedical.model.dialyse;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "dialysis_prescriptions")
public class DialysisPrescription {
    @Id
    private String id;
    private String patientId;
    private String patientName;
    private String medecinId;
    private TypeDialyse type;
    private Integer frequencyPerWeek;      // Frequency per week (e.g., 3 times/week)
    private Integer sessionDurationMinutes; // Duration in minutes
    private Double bloodFlowRate;          // Blood flow rate (ml/min)
    private Double dialysateFlowRate;      // Dialysate flow rate (ml/min)
    private String anticoagulation;        // Anticoagulation protocol
    private String vascularAccess;         // Vascular access type
    private String notes;                  // Additional notes
    private LocalDateTime startDate;       // Prescription start date
    private LocalDateTime endDate;         // Prescription end date (optional)
    private Boolean active;                // Is prescription active
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
