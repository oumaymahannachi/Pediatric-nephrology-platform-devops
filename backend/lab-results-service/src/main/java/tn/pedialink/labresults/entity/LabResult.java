package tn.pedialink.labresults.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "lab_results")
public class LabResult {

    @Id
    private String id;
    private String patientId;
    private String doctorId;
    private LocalDateTime testDate;
    private TestType testType;

    private Double creatinine;
    private Double urea;
    private Double sodium;
    private Double potassium;
    private Double calcium;
    private Double phosphorus;
    private Double hemoglobin;
    private Double albumin;
    private Double bicarbonate;

    private Double urineProtein;
    private Double urineCreatinine;
    private String urineAspect;
    private Double urineAlbumin;

    private Double eGFR;
    private CKDStage ckdStage;
    private Double proteinCreatinineRatio;

    private String doctorNotes;
    private Boolean isAbnormal = false;
    private List<String> alerts = new ArrayList<>();
    private String laboratoryName;
    private ResultStatus status = ResultStatus.PENDING;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    public LabResult() {
        this.alerts = new ArrayList<>();
        this.isAbnormal = false;
        this.status = ResultStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public LocalDateTime getTestDate() { return testDate; }
    public void setTestDate(LocalDateTime testDate) { this.testDate = testDate; }

    public TestType getTestType() { return testType; }
    public void setTestType(TestType testType) { this.testType = testType; }

    public Double getCreatinine() { return creatinine; }
    public void setCreatinine(Double creatinine) { this.creatinine = creatinine; }

    public Double getUrea() { return urea; }
    public void setUrea(Double urea) { this.urea = urea; }

    public Double getSodium() { return sodium; }
    public void setSodium(Double sodium) { this.sodium = sodium; }

    public Double getPotassium() { return potassium; }
    public void setPotassium(Double potassium) { this.potassium = potassium; }

    public Double getCalcium() { return calcium; }
    public void setCalcium(Double calcium) { this.calcium = calcium; }

    public Double getPhosphorus() { return phosphorus; }
    public void setPhosphorus(Double phosphorus) { this.phosphorus = phosphorus; }

    public Double getHemoglobin() { return hemoglobin; }
    public void setHemoglobin(Double hemoglobin) { this.hemoglobin = hemoglobin; }

    public Double getAlbumin() { return albumin; }
    public void setAlbumin(Double albumin) { this.albumin = albumin; }

    public Double getBicarbonate() { return bicarbonate; }
    public void setBicarbonate(Double bicarbonate) { this.bicarbonate = bicarbonate; }

    public Double getUrineProtein() { return urineProtein; }
    public void setUrineProtein(Double urineProtein) { this.urineProtein = urineProtein; }

    public Double getUrineCreatinine() { return urineCreatinine; }
    public void setUrineCreatinine(Double urineCreatinine) { this.urineCreatinine = urineCreatinine; }

    public String getUrineAspect() { return urineAspect; }
    public void setUrineAspect(String urineAspect) { this.urineAspect = urineAspect; }

    public Double getUrineAlbumin() { return urineAlbumin; }
    public void setUrineAlbumin(Double urineAlbumin) { this.urineAlbumin = urineAlbumin; }

    public Double getEGFR() { return eGFR; }
    public void setEGFR(Double eGFR) { this.eGFR = eGFR; }

    public CKDStage getCkdStage() { return ckdStage; }
    public void setCkdStage(CKDStage ckdStage) { this.ckdStage = ckdStage; }

    public Double getProteinCreatinineRatio() { return proteinCreatinineRatio; }
    public void setProteinCreatinineRatio(Double proteinCreatinineRatio) { this.proteinCreatinineRatio = proteinCreatinineRatio; }

    public String getDoctorNotes() { return doctorNotes; }
    public void setDoctorNotes(String doctorNotes) { this.doctorNotes = doctorNotes; }

    public Boolean getIsAbnormal() { return isAbnormal; }
    public void setIsAbnormal(Boolean isAbnormal) { this.isAbnormal = isAbnormal; }

    public List<String> getAlerts() { return alerts; }
    public void setAlerts(List<String> alerts) { this.alerts = alerts; }

    public String getLaboratoryName() { return laboratoryName; }
    public void setLaboratoryName(String laboratoryName) { this.laboratoryName = laboratoryName; }

    public ResultStatus getStatus() { return status; }
    public void setStatus(ResultStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    // Builder pattern
    public static LabResultBuilder builder() { return new LabResultBuilder(); }

    public static class LabResultBuilder {
        private final LabResult result = new LabResult();

        public LabResultBuilder patientId(String v) { result.patientId = v; return this; }
        public LabResultBuilder doctorId(String v) { result.doctorId = v; return this; }
        public LabResultBuilder testDate(LocalDateTime v) { result.testDate = v; return this; }
        public LabResultBuilder testType(TestType v) { result.testType = v; return this; }
        public LabResultBuilder creatinine(Double v) { result.creatinine = v; return this; }
        public LabResultBuilder urea(Double v) { result.urea = v; return this; }
        public LabResultBuilder sodium(Double v) { result.sodium = v; return this; }
        public LabResultBuilder potassium(Double v) { result.potassium = v; return this; }
        public LabResultBuilder calcium(Double v) { result.calcium = v; return this; }
        public LabResultBuilder phosphorus(Double v) { result.phosphorus = v; return this; }
        public LabResultBuilder hemoglobin(Double v) { result.hemoglobin = v; return this; }
        public LabResultBuilder albumin(Double v) { result.albumin = v; return this; }
        public LabResultBuilder bicarbonate(Double v) { result.bicarbonate = v; return this; }
        public LabResultBuilder urineProtein(Double v) { result.urineProtein = v; return this; }
        public LabResultBuilder urineCreatinine(Double v) { result.urineCreatinine = v; return this; }
        public LabResultBuilder urineAspect(String v) { result.urineAspect = v; return this; }
        public LabResultBuilder urineAlbumin(Double v) { result.urineAlbumin = v; return this; }
        public LabResultBuilder eGFR(Double v) { result.eGFR = v; return this; }
        public LabResultBuilder ckdStage(CKDStage v) { result.ckdStage = v; return this; }
        public LabResultBuilder proteinCreatinineRatio(Double v) { result.proteinCreatinineRatio = v; return this; }
        public LabResultBuilder doctorNotes(String v) { result.doctorNotes = v; return this; }
        public LabResultBuilder isAbnormal(Boolean v) { result.isAbnormal = v; return this; }
        public LabResultBuilder alerts(List<String> v) { result.alerts = v; return this; }
        public LabResultBuilder laboratoryName(String v) { result.laboratoryName = v; return this; }
        public LabResultBuilder status(ResultStatus v) { result.status = v; return this; }
        public LabResultBuilder createdAt(LocalDateTime v) { result.createdAt = v; return this; }
        public LabResultBuilder createdBy(String v) { result.createdBy = v; return this; }

        public LabResult build() { return result; }
    }
}
