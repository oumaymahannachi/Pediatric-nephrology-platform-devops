package tn.pedialink.prescription.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.pedialink.prescription.dto.ApiResponse;
import tn.pedialink.prescription.dto.adherence.AdherenceLogRequest;
import tn.pedialink.prescription.model.AdherenceLog;
import tn.pedialink.prescription.service.AdherenceService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/adherence")
@RequiredArgsConstructor
public class AdherenceController {
    
    private final AdherenceService adherenceService;
    
    @PostMapping("/log")
    public ResponseEntity<ApiResponse<AdherenceLog>> logPriseMedicament(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @Valid @RequestBody AdherenceLogRequest request) {
        
        String patientId = userId != null ? userId : "patient-test";
        AdherenceLog log = adherenceService.logPriseMedicament(patientId, request);
        return ResponseEntity.ok(ApiResponse.ok("Prise enregistrée avec succès", log));
    }
    
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<List<AdherenceLog>>> getLogsPatient(@PathVariable String patientId) {
        List<AdherenceLog> logs = adherenceService.getLogsPatient(patientId);
        return ResponseEntity.ok(ApiResponse.ok(logs));
    }
    
    @GetMapping("/prescription/{prescriptionId}")
    public ResponseEntity<ApiResponse<List<AdherenceLog>>> getLogsPrescription(@PathVariable String prescriptionId) {
        List<AdherenceLog> logs = adherenceService.getLogsPrescription(prescriptionId);
        return ResponseEntity.ok(ApiResponse.ok(logs));
    }
    
    @GetMapping("/stats/{patientId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistiques(
            @PathVariable String patientId,
            @RequestParam(defaultValue = "30") int jours) {
        
        Map<String, Object> stats = adherenceService.calculerStatistiquesAdherence(patientId, jours);
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }
}
