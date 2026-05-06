package tn.pedialink.dossiermedical.model.examen;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "medical_imaging")
public class MedicalImaging {
    @Id
    private String id;
    private String patientId;
    private String patientName;
    private String medecinId;
    private String medecinName;
    private LocalDateTime imagingDate;
    private String imagingType; // XRAY, ULTRASOUND, CT_SCAN, MRI, etc.
    private String bodyPart;
    private String indication; // Reason for imaging
    
    // Results
    private String findings;
    private String impression; // Radiologist's impression
    private String recommendation;
    
    private String radiologistName;
    private String performedBy; // Who performed the examination
    private String facilityName;
    private List<String> imageUrls; // URLs to stored images
    private List<String> documentUrls; // URLs to PDFs
    private String urgencyLevel; // NORMAL, URGENT, CRITICAL
    private Boolean followUpRequired;
    private LocalDateTime followUpDate;
    private String status; // PENDING, COMPLETED, REVIEWED
    private String notes;
    private Boolean abnormal;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
