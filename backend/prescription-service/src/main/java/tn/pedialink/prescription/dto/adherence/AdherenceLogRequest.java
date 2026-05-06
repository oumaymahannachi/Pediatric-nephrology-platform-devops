package tn.pedialink.prescription.dto.adherence;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdherenceLogRequest {
    @NotNull
    private String prescriptionId;
    
    @NotNull
    private String medicamentNom;
    
    @NotNull
    private LocalDateTime datePrise;
    
    private Boolean prise; // true = pris, false = oublié
    
    private String raison; // raison si oublié
    
    private String notes;
    
    private String effetsSecondaires;
}
