package tn.pedialink.labresults.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.pedialink.labresults.entity.CKDStage;
import tn.pedialink.labresults.entity.ResultStatus;
import tn.pedialink.labresults.entity.TestType;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabResultDto {
    private String id;
    private String patientId;
    private String doctorId;
    private LocalDateTime testDate;
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
    
    // Calculated values
    private Double eGFR;
    private CKDStage ckdStage;
    private Double proteinCreatinineRatio;
    
    // Metadata
    private String doctorNotes;
    private Boolean isAbnormal;
    private List<String> alerts;
    private String laboratoryName;
    private ResultStatus status;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
