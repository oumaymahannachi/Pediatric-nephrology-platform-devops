package tn.pedialink.dossiermedical.model.examen;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "lab_results")
public class LabResult {
    @Id
    private String id;
    private String patientId;
    private String patientName;
    private String medecinId;
    private String medecinName;
    private LocalDateTime testDate;
    private String testType; // URINALYSIS, CULTURE, BIOPSY, etc.
    private String testName;
    
    // Results
    private String findings;
    private String result; // POSITIVE, NEGATIVE, NORMAL, ABNORMAL
    private String details;
    
    private String laboratoryName;
    private String specimenType; // Urine, Blood, Tissue, etc.
    private String notes;
    private Boolean abnormal;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
