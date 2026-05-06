package tn.pedialink.dossiermedical.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class BloodTestDto {
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
    private Map<String, TestValueDto> results;
    private String laboratoryName;
    private String notes;
    private String interpretation;
    private Boolean abnormal;
    
    @Data
    public static class TestValueDto {
        private Double value;
        private String unit;
        private String referenceRange;
        private Boolean isAbnormal;
    }
}
