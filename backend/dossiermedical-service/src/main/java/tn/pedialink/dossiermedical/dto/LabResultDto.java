package tn.pedialink.dossiermedical.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LabResultDto {
    @NotBlank
    private String patientId;
    private String patientName;
    @NotBlank
    private String medecinId;
    private String medecinName;
    @NotNull
    private LocalDateTime testDate;
    @NotBlank
    private String testType;
    @NotBlank
    private String testName;
    private String findings;
    private String result;
    private String details;
    private String laboratoryName;
    private String specimenType;
    private String notes;
    private Boolean abnormal;
}
