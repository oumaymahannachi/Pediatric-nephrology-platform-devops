package tn.pedialink.dossiermedical.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MedicalImagingDto {
    @NotBlank
    private String patientId;
    private String patientName;
    @NotBlank
    private String medecinId;
    private String medecinName;
    @NotNull
    private LocalDateTime imagingDate;
    @NotBlank
    private String imagingType;
    @NotBlank
    private String bodyPart;
    private String indication;
    private String findings;
    private String impression;
    private String recommendation;
    private String radiologistName;
    private String performedBy;
    private String facilityName;
    private List<String> imageUrls;
    private List<String> documentUrls;
    private String urgencyLevel; // NORMAL, URGENT, CRITICAL
    private Boolean followUpRequired;
    private LocalDateTime followUpDate;
    private String status; // PENDING, COMPLETED, REVIEWED
    private String notes;
    private Boolean abnormal;
}
