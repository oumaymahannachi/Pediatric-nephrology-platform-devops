package tn.pedialink.dossiermedical.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import tn.pedialink.dossiermedical.model.dialyse.StatutSession;
import java.time.LocalDateTime;

@Data
public class DialysisSessionDto {
    @NotBlank
    private String prescriptionId;
    @NotBlank
    private String patientId;
    private String patientName;
    @NotBlank
    private String medecinId;
    @NotNull
    private LocalDateTime scheduledDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private StatutSession status;
    
    // Pre-dialysis vitals
    private Double preWeight;
    private Double preBloodPressureSystolic;
    private Double preBloodPressureDiastolic;
    private Double prePulse;
    private Double preTemperature;
    
    // Post-dialysis vitals
    private Double postWeight;
    private Double postBloodPressureSystolic;
    private Double postBloodPressureDiastolic;
    private Double postPulse;
    private Double postTemperature;
    
    // Session details
    private Double ultrafiltrationVolume;
    private String complications;
    private String notes;
}
