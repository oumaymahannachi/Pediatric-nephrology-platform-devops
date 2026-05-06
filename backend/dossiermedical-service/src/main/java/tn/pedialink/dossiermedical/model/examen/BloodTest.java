package tn.pedialink.dossiermedical.model.examen;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Document(collection = "blood_tests")
public class BloodTest {
    @Id
    private String id;
    private String patientId;
    private String patientName;
    private String medecinId;
    private String medecinName;
    private LocalDateTime testDate;
    private String testType; // CBC, METABOLIC_PANEL, KIDNEY_FUNCTION, etc.
    
    // Common blood test values
    private Map<String, TestValue> results; // Key: test name, Value: result with unit and reference range
    
    private String laboratoryName;
    private String notes;
    private String interpretation; // Doctor's interpretation
    private Boolean abnormal; // Flag for abnormal results
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    public static class TestValue {
        private Double value;
        private String unit;
        private String referenceRange;
        private Boolean isAbnormal;
    }
}
