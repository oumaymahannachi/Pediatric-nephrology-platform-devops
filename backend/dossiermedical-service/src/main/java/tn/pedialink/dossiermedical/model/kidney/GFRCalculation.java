package tn.pedialink.dossiermedical.model.kidney;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "gfr_calculations")
public class GFRCalculation {
    @Id
    private String id;
    private String patientId;
    private String patientName;
    private Double heightCm; // Taille en cm
    private Double serumCreatinine; // Créatinine sérique en mg/dL
    private Double gfrValue; // Résultat DFG
    private CKDStage ckdStage; // Stade CKD
    private String interpretation;
    private LocalDateTime calculationDate;
    private String medecinId;
    private String notes;
}
