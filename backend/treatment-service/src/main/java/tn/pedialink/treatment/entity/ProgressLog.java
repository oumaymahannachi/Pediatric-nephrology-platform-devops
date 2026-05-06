package tn.pedialink.treatment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "progress_logs")
public class ProgressLog {
    @Id
    private String id;
    
    private String treatmentId;
    private String patientId;
    private LocalDate date;
    private Map<String, Integer> symptomes;
    private String notes;
    private Double poids;
    private Double taille;
    private String humeur;
    private Integer niveauEnergie;
    private Integer qualiteSommeil;
    
    private LocalDateTime createdAt;
}
