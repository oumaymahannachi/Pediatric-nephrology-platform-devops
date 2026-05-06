package tn.pedialink.prescription.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "adherence_logs")
public class AdherenceLog {
    @Id
    private String id;
    
    private String prescriptionId;
    private String patientId;
    private String medicamentNom;
    private LocalDateTime datePrise;
    private Boolean prise;
    private String raison;
    private String notes;
    private String effetsSecondaires;
    
    private LocalDateTime createdAt;
}
