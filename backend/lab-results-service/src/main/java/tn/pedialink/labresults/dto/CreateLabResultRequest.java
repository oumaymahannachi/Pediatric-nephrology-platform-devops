package tn.pedialink.labresults.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.pedialink.labresults.entity.TestType;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLabResultRequest {
    
    @NotBlank(message = "Patient ID is required")
    private String patientId;
    
    @NotNull(message = "Test date is required")
    private LocalDateTime testDate;
    
    @NotNull(message = "Test type is required")
    private TestType testType;
    
    // Blood tests
    private Double creatinine;
    private Double urea;
    private Double sodium;
    private Double potassium;
    private Double calcium;
    private Double phosphorus;
    private Double hemoglobin;
    private Double albumin;
    private Double bicarbonate;
    
    // Urine tests
    private Double urineProtein;
    private Double urineCreatinine;
    private String urineAspect;
    private Double urineAlbumin;
    
    // Metadata
    private String doctorNotes;
    private String laboratoryName;
    
    // Parent Contact Info for Notifications
    private String parentEmail;
    private String parentPhone;
    private Boolean sendEmailNotification;
    private Boolean sendSmsNotification;
}