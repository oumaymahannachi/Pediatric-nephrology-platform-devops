package tn.pedialink.labresults.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.pedialink.labresults.dto.ApiResponse;
import tn.pedialink.labresults.dto.CreateLabResultRequest;
import tn.pedialink.labresults.dto.LabResultDto;
import tn.pedialink.labresults.entity.TestType;
import tn.pedialink.labresults.service.LabResultService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/lab-results")
@RequiredArgsConstructor
public class LabResultController {
    
    private final LabResultService labResultService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<LabResultDto>> createLabResult(
            @Valid @RequestBody CreateLabResultRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        log.info("Creating lab result for patient: {}", request.getPatientId());
        
        String doctorId = userId != null ? userId : "doctor-1"; // Default for testing
        LabResultDto result = labResultService.createLabResult(request, doctorId);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Lab result created successfully", result));
    }
    
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<List<LabResultDto>>> getLabResultsByPatient(
            @PathVariable String patientId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Fetching lab results for patient: {} by user: {} (role: {})", patientId, userId, userRole);

        // Vérification des permissions:
        // - DOCTOR: peut voir tous les patients
        // - PARENT: peut voir seulement ses propres enfants (patientId doit correspondre)
        // - Pas de rôle: accès refusé en production (ici on permet pour les tests)
        if (userRole != null && userRole.equals("PARENT") && userId != null) {
            // Le parent ne peut accéder qu'aux données de ses propres enfants
            // La vérification complète se fait via le treatment-monitoring-service
            // qui connaît la relation parent-enfant
            log.info("Parent {} accessing patient {} data", userId, patientId);
        }

        List<LabResultDto> results = labResultService.getLabResultsByPatient(patientId);
        return ResponseEntity.ok(ApiResponse.success(results));
    }
    
    @GetMapping("/patient/{patientId}/type/{testType}")
    public ResponseEntity<ApiResponse<List<LabResultDto>>> getLabResultsByPatientAndType(
            @PathVariable String patientId,
            @PathVariable TestType testType) {
        
        log.info("Fetching {} results for patient: {}", testType, patientId);
        List<LabResultDto> results = labResultService.getLabResultsByPatientAndType(patientId, testType);
        
        return ResponseEntity.ok(ApiResponse.success(results));
    }
    
    @GetMapping("/patient/{patientId}/abnormal")
    public ResponseEntity<ApiResponse<List<LabResultDto>>> getAbnormalLabResults(
            @PathVariable String patientId) {
        
        log.info("Fetching abnormal results for patient: {}", patientId);
        List<LabResultDto> results = labResultService.getAbnormalLabResults(patientId);
        
        return ResponseEntity.ok(ApiResponse.success(results));
    }
    
    @GetMapping("/patient/{patientId}/date-range")
    public ResponseEntity<ApiResponse<List<LabResultDto>>> getLabResultsByDateRange(
            @PathVariable String patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("Fetching results for patient {} between {} and {}", patientId, startDate, endDate);
        List<LabResultDto> results = labResultService.getLabResultsByDateRange(patientId, startDate, endDate);
        
        return ResponseEntity.ok(ApiResponse.success(results));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LabResultDto>> getLabResultById(@PathVariable String id) {
        log.info("Fetching lab result: {}", id);
        LabResultDto result = labResultService.getLabResultById(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LabResultDto>> updateLabResult(
            @PathVariable String id,
            @Valid @RequestBody CreateLabResultRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        log.info("Updating lab result: {}", id);
        String doctorId = userId != null ? userId : "doctor-1";
        LabResultDto result = labResultService.updateLabResult(id, request, doctorId);
        
        return ResponseEntity.ok(ApiResponse.success("Lab result updated successfully", result));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLabResult(@PathVariable String id) {
        log.info("Deleting lab result: {}", id);
        labResultService.deleteLabResult(id);
        return ResponseEntity.ok(ApiResponse.success("Lab result deleted successfully", null));
    }
    
    @PutMapping("/{id}/validate")
    public ResponseEntity<ApiResponse<LabResultDto>> validateLabResult(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        log.info("Validating lab result: {}", id);
        String doctorId = userId != null ? userId : "doctor-1";
        LabResultDto result = labResultService.validateLabResult(id, doctorId);
        
        return ResponseEntity.ok(ApiResponse.success("Lab result validated successfully", result));
    }
}
