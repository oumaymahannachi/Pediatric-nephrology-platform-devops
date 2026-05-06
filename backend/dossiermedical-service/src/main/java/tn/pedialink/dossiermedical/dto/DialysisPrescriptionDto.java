package tn.pedialink.dossiermedical.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import tn.pedialink.dossiermedical.model.dialyse.TypeDialyse;
import java.time.LocalDateTime;

@Data
public class DialysisPrescriptionDto {
    @NotBlank
    private String patientId;
    private String patientName;
    @NotBlank
    private String medecinId;
    @NotNull
    private TypeDialyse type;
    @NotNull
    private Integer frequencyPerWeek;
    @NotNull
    private Integer sessionDurationMinutes;
    private Double bloodFlowRate;
    private Double dialysateFlowRate;
    private String anticoagulation;
    private String vascularAccess;
    private String notes;
    @NotNull
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
