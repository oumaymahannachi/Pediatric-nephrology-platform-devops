package tn.pedialink.labresults.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.pedialink.labresults.dto.CreateLabResultRequest;
import tn.pedialink.labresults.dto.LabResultDto;
import tn.pedialink.labresults.dto.NotificationRequest;
import tn.pedialink.labresults.entity.CKDStage;
import tn.pedialink.labresults.entity.LabResult;
import tn.pedialink.labresults.entity.ResultStatus;
import tn.pedialink.labresults.entity.TestType;
import tn.pedialink.labresults.repository.LabResultRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LabResultService {
    
    private final LabResultRepository labResultRepository;
    private final EGFRCalculationService egfrCalculationService;
    private final AlertService alertService;
    private final EmailNotificationService emailNotificationService;
    
    public LabResultDto createLabResult(CreateLabResultRequest request, String doctorId) {
        log.info("Creating lab result for patient: {}", request.getPatientId());
        
        LabResult labResult = LabResult.builder()
                .patientId(request.getPatientId())
                .doctorId(doctorId)
                .testDate(request.getTestDate())
                .testType(request.getTestType())
                .creatinine(request.getCreatinine())
                .urea(request.getUrea())
                .sodium(request.getSodium())
                .potassium(request.getPotassium())
                .calcium(request.getCalcium())
                .phosphorus(request.getPhosphorus())
                .hemoglobin(request.getHemoglobin())
                .albumin(request.getAlbumin())
                .bicarbonate(request.getBicarbonate())
                .urineProtein(request.getUrineProtein())
                .urineCreatinine(request.getUrineCreatinine())
                .urineAspect(request.getUrineAspect())
                .urineAlbumin(request.getUrineAlbumin())
                .doctorNotes(request.getDoctorNotes())
                .laboratoryName(request.getLaboratoryName())
                .status(ResultStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .createdBy(doctorId)
                .build();
        
        // Calculate eGFR (need patient height - for now using default 120cm for demo)
        // In production, fetch patient data from dossier-medical-service
        if (labResult.getCreatinine() != null) {
            Double eGFR = egfrCalculationService.calculateEGFR(
                    labResult.getCreatinine(), 120.0, true);
            labResult.setEGFR(eGFR);
            
            CKDStage stage = egfrCalculationService.determineCKDStage(eGFR);
            labResult.setCkdStage(stage);
        }
        
        // Calculate protein/creatinine ratio
        if (labResult.getUrineProtein() != null && labResult.getUrineCreatinine() != null) {
            Double ratio = egfrCalculationService.calculateProteinCreatinineRatio(
                    labResult.getUrineProtein(), labResult.getUrineCreatinine());
            labResult.setProteinCreatinineRatio(ratio);
        }
        
        // Generate alerts
        List<String> alerts = alertService.generateAlerts(labResult);
        labResult.setAlerts(alerts);
        labResult.setIsAbnormal(alertService.hasAbnormalValues(labResult));
        
        LabResult saved = labResultRepository.save(labResult);
        log.info("Lab result created with ID: {}", saved.getId());
        
        // 🔔 Send email notification if requested
        sendEmailNotificationIfRequested(saved, request);
        
        return mapToDto(saved);
    }
    
    private void sendEmailNotificationIfRequested(LabResult labResult, CreateLabResultRequest request) {
        try {
            // Check if email notification is requested
            if (request.getSendEmailNotification() != null && 
                request.getSendEmailNotification() && 
                request.getParentEmail() != null && 
                !request.getParentEmail().trim().isEmpty()) {
                
                log.info("📧 Sending email notification to: {}", request.getParentEmail());
                
                NotificationRequest notificationRequest = new NotificationRequest();
                notificationRequest.setRecipientEmail(request.getParentEmail().trim());
                notificationRequest.setRecipientPhone(request.getParentPhone());
                notificationRequest.setPatientName("Patient"); // TODO: Get from patient service
                notificationRequest.setTestType(labResult.getTestType().toString());
                notificationRequest.setTestDate(labResult.getTestDate().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                notificationRequest.setAbnormal(labResult.getIsAbnormal());
                notificationRequest.setDoctorName("Doctor"); // TODO: Get from auth service

                // Determine if critical
                boolean isCritical = false;
                if (labResult.getAlerts() != null && !labResult.getAlerts().isEmpty()) {
                    isCritical = labResult.getAlerts().stream()
                            .anyMatch(alert -> alert.toLowerCase().contains("critical") || 
                                             alert.toLowerCase().contains("severe") ||
                                             alert.toLowerCase().contains("urgent"));
                }
                
                // Send appropriate notification
                if (isCritical) {
                    log.warn("⚠️ Sending CRITICAL alert email to: {}", request.getParentEmail());
                    emailNotificationService.sendCriticalResultAlert(notificationRequest);
                } else {
                    emailNotificationService.sendLabResultNotification(notificationRequest);
                }

                log.info("✅ Email notification sent successfully to: {}", request.getParentEmail());

            } else {
                log.info("ℹ️ Email notification not requested or no email provided");
            }

        } catch (Exception e) {
            log.error("❌ Failed to send email notification: {}", e.getMessage(), e);
            // Don't fail the lab result creation if notification fails
        }
    }
    
    public List<LabResultDto> getLabResultsByPatient(String patientId) {
        log.info("Fetching lab results for patient: {}", patientId);
        List<LabResult> results = labResultRepository.findByPatientIdOrderByTestDateDesc(patientId);
        return results.stream().map(this::mapToDto).collect(Collectors.toList());
    }
    
    public List<LabResultDto> getLabResultsByPatientAndType(String patientId, TestType testType) {
        log.info("Fetching {} lab results for patient: {}", testType, patientId);
        List<LabResult> results = labResultRepository
                .findByPatientIdAndTestTypeOrderByTestDateDesc(patientId, testType);
        return results.stream().map(this::mapToDto).collect(Collectors.toList());
    }
    
    public List<LabResultDto> getAbnormalLabResults(String patientId) {
        log.info("Fetching abnormal lab results for patient: {}", patientId);
        List<LabResult> results = labResultRepository
                .findByPatientIdAndIsAbnormalTrueOrderByTestDateDesc(patientId);
        return results.stream().map(this::mapToDto).collect(Collectors.toList());
    }
    
    public List<LabResultDto> getLabResultsByDateRange(String patientId, 
                                                        LocalDateTime startDate, 
                                                        LocalDateTime endDate) {
        log.info("Fetching lab results for patient {} between {} and {}", 
                patientId, startDate, endDate);
        List<LabResult> results = labResultRepository
                .findByPatientIdAndTestDateBetweenOrderByTestDateDesc(patientId, startDate, endDate);
        return results.stream().map(this::mapToDto).collect(Collectors.toList());
    }
    
    public LabResultDto getLabResultById(String id) {
        log.info("Fetching lab result: {}", id);
        LabResult result = labResultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lab result not found"));
        return mapToDto(result);
    }
    
    public LabResultDto updateLabResult(String id, CreateLabResultRequest request, String doctorId) {
        log.info("Updating lab result: {}", id);
        
        LabResult existing = labResultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lab result not found"));
        
        // Update fields
        existing.setTestDate(request.getTestDate());
        existing.setTestType(request.getTestType());
        existing.setCreatinine(request.getCreatinine());
        existing.setUrea(request.getUrea());
        existing.setSodium(request.getSodium());
        existing.setPotassium(request.getPotassium());
        existing.setCalcium(request.getCalcium());
        existing.setPhosphorus(request.getPhosphorus());
        existing.setHemoglobin(request.getHemoglobin());
        existing.setAlbumin(request.getAlbumin());
        existing.setBicarbonate(request.getBicarbonate());
        existing.setUrineProtein(request.getUrineProtein());
        existing.setUrineCreatinine(request.getUrineCreatinine());
        existing.setUrineAspect(request.getUrineAspect());
        existing.setUrineAlbumin(request.getUrineAlbumin());
        existing.setDoctorNotes(request.getDoctorNotes());
        existing.setLaboratoryName(request.getLaboratoryName());
        existing.setStatus(ResultStatus.REVIEWED);
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(doctorId);
        
        // Recalculate eGFR
        if (existing.getCreatinine() != null) {
            Double eGFR = egfrCalculationService.calculateEGFR(
                    existing.getCreatinine(), 120.0, true);
            existing.setEGFR(eGFR);
            existing.setCkdStage(egfrCalculationService.determineCKDStage(eGFR));
        }
        
        // Recalculate protein/creatinine ratio
        if (existing.getUrineProtein() != null && existing.getUrineCreatinine() != null) {
            Double ratio = egfrCalculationService.calculateProteinCreatinineRatio(
                    existing.getUrineProtein(), existing.getUrineCreatinine());
            existing.setProteinCreatinineRatio(ratio);
        }
        
        // Regenerate alerts
        List<String> alerts = alertService.generateAlerts(existing);
        existing.setAlerts(alerts);
        existing.setIsAbnormal(alertService.hasAbnormalValues(existing));
        
        LabResult updated = labResultRepository.save(existing);
        log.info("Lab result updated: {}", id);
        
        return mapToDto(updated);
    }
    
    public void deleteLabResult(String id) {
        log.info("Deleting lab result: {}", id);
        labResultRepository.deleteById(id);
    }

    // ===== FONCTIONNALITÉS AVANCÉES =====

    /**
     * Analyse la tendance de l'eGFR d'un patient sur le temps.
     * Retourne: évolution, vitesse de dégradation, prédiction stade suivant.
     */
    public java.util.Map<String, Object> analyzeEGFRTrend(String patientId) {
        log.info("Analyzing eGFR trend for patient: {}", patientId);

        List<LabResult> results = labResultRepository.findByPatientIdOrderByTestDateDesc(patientId)
                .stream()
                .filter(r -> r.getEGFR() != null)
                .collect(Collectors.toList());

        if (results.isEmpty()) {
            return java.util.Map.of("message", "No eGFR data available");
        }

        // Calculer la tendance
        double latestEGFR = results.get(0).getEGFR();
        String trend = "STABLE";
        double changeRate = 0.0;

        if (results.size() >= 2) {
            double previousEGFR = results.get(1).getEGFR();
            changeRate = latestEGFR - previousEGFR;
            if (changeRate < -5) trend = "DECLINING";
            else if (changeRate > 5) trend = "IMPROVING";
        }

        // Déterminer le risque
        String riskLevel = "LOW";
        if (latestEGFR < 30) riskLevel = "HIGH";
        else if (latestEGFR < 60) riskLevel = "MODERATE";

        // Prédiction: si la tendance continue, quand atteindra-t-il le stade suivant?
        String prediction = "Stable - no immediate risk";
        if (trend.equals("DECLINING") && changeRate != 0) {
            double monthsToNextStage = 0;
            if (latestEGFR > 60) monthsToNextStage = (latestEGFR - 60) / Math.abs(changeRate);
            else if (latestEGFR > 30) monthsToNextStage = (latestEGFR - 30) / Math.abs(changeRate);
            else if (latestEGFR > 15) monthsToNextStage = (latestEGFR - 15) / Math.abs(changeRate);
            if (monthsToNextStage > 0) {
                prediction = String.format("At current rate, may reach next CKD stage in ~%.0f months", monthsToNextStage);
            }
        }

        return java.util.Map.of(
            "patientId", patientId,
            "latestEGFR", latestEGFR,
            "currentCKDStage", egfrCalculationService.determineCKDStage(latestEGFR).toString(),
            "trend", trend,
            "changeRate", changeRate,
            "riskLevel", riskLevel,
            "prediction", prediction,
            "totalMeasurements", results.size()
        );
    }

    /**
     * Génère un rapport médical complet du patient avec toutes ses valeurs et alertes.
     */
    public java.util.Map<String, Object> generatePatientReport(String patientId) {
        log.info("Generating medical report for patient: {}", patientId);

        List<LabResult> allResults = labResultRepository.findByPatientIdOrderByTestDateDesc(patientId);

        if (allResults.isEmpty()) {
            return java.util.Map.of("message", "No lab results found for patient");
        }

        LabResult latest = allResults.get(0);
        long abnormalCount = allResults.stream().filter(r -> Boolean.TRUE.equals(r.getIsAbnormal())).count();
        long criticalCount = allResults.stream()
            .filter(r -> r.getAlerts() != null && r.getAlerts().stream().anyMatch(a -> a.contains("URGENT") || a.contains("CRITICAL")))
            .count();

        // Valeurs moyennes
        double avgEGFR = allResults.stream()
            .filter(r -> r.getEGFR() != null)
            .mapToDouble(LabResult::getEGFR)
            .average().orElse(0);

        return java.util.Map.of(
            "patientId", patientId,
            "totalResults", allResults.size(),
            "abnormalResults", abnormalCount,
            "criticalAlerts", criticalCount,
            "latestEGFR", latest.getEGFR() != null ? latest.getEGFR() : "N/A",
            "latestCKDStage", latest.getCkdStage() != null ? latest.getCkdStage().toString() : "UNKNOWN",
            "averageEGFR", Math.round(avgEGFR * 100.0) / 100.0,
            "lastTestDate", latest.getTestDate() != null ? latest.getTestDate().toString() : "N/A",
            "currentAlerts", latest.getAlerts() != null ? latest.getAlerts() : java.util.List.of()
        );
    }
    
    public LabResultDto validateLabResult(String id, String doctorId) {
        log.info("Validating lab result: {}", id);
        
        LabResult result = labResultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lab result not found"));
        
        result.setStatus(ResultStatus.VALIDATED);
        result.setUpdatedAt(LocalDateTime.now());
        result.setUpdatedBy(doctorId);
        
        LabResult validated = labResultRepository.save(result);
        return mapToDto(validated);
    }
    
    private LabResultDto mapToDto(LabResult labResult) {
        return LabResultDto.builder()
                .id(labResult.getId())
                .patientId(labResult.getPatientId())
                .doctorId(labResult.getDoctorId())
                .testDate(labResult.getTestDate())
                .testType(labResult.getTestType())
                .creatinine(labResult.getCreatinine())
                .urea(labResult.getUrea())
                .sodium(labResult.getSodium())
                .potassium(labResult.getPotassium())
                .calcium(labResult.getCalcium())
                .phosphorus(labResult.getPhosphorus())
                .hemoglobin(labResult.getHemoglobin())
                .albumin(labResult.getAlbumin())
                .bicarbonate(labResult.getBicarbonate())
                .urineProtein(labResult.getUrineProtein())
                .urineCreatinine(labResult.getUrineCreatinine())
                .urineAspect(labResult.getUrineAspect())
                .urineAlbumin(labResult.getUrineAlbumin())
                .eGFR(labResult.getEGFR())
                .ckdStage(labResult.getCkdStage())
                .proteinCreatinineRatio(labResult.getProteinCreatinineRatio())
                .doctorNotes(labResult.getDoctorNotes())
                .isAbnormal(labResult.getIsAbnormal())
                .alerts(labResult.getAlerts())
                .laboratoryName(labResult.getLaboratoryName())
                .status(labResult.getStatus())
                .createdAt(labResult.getCreatedAt())
                .updatedAt(labResult.getUpdatedAt())
                .build();
    }
}
