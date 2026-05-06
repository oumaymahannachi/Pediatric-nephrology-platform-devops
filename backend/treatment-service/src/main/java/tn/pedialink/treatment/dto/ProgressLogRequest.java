package tn.pedialink.treatment.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.Map;

@Data
public class ProgressLogRequest {
    private String treatmentId;
    private LocalDate date;
    private Map<String, Integer> symptomes; // nom symptome -> severite (1-10)
    private String notes;
    private Double poids;
    private Double taille;
    private String humeur;
    private Integer niveauEnergie; // 1-10
    private Integer qualiteSommeil; // 1-10
}
