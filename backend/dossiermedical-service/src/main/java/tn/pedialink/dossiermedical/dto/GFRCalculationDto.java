package tn.pedialink.dossiermedical.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class GFRCalculationDto {
    @NotNull
    private String patientId;
    private String patientName;
    
    @NotNull
    @Positive
    private Double heightCm;
    
    @NotNull
    @Positive
    private Double serumCreatinine;
    
    @NotNull
    private String medecinId;
    
    private String notes;
}
