package tn.pedialink.treatment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.pedialink.treatment.dto.ApiResponse;
import tn.pedialink.treatment.dto.ProgressLogRequest;
import tn.pedialink.treatment.entity.ProgressLog;
import tn.pedialink.treatment.service.ProgressService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/progress")
@RequiredArgsConstructor
public class ProgressController {
    
    private final ProgressService progressService;
    
    @PostMapping("/log")
    public ResponseEntity<ApiResponse<ProgressLog>> logProgress(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody ProgressLogRequest request) {
        
        String patientId = userId != null ? userId : "patient-test";
        ProgressLog log = progressService.logProgress(patientId, request);
        return ResponseEntity.ok(ApiResponse.ok("Progrès enregistré avec succès", log));
    }
    
    @GetMapping("/treatment/{treatmentId}")
    public ResponseEntity<ApiResponse<List<ProgressLog>>> getProgressByTreatment(@PathVariable String treatmentId) {
        List<ProgressLog> logs = progressService.getProgressByTreatment(treatmentId);
        return ResponseEntity.ok(ApiResponse.ok(logs));
    }
    
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<List<ProgressLog>>> getProgressByPatient(@PathVariable String patientId) {
        List<ProgressLog> logs = progressService.getProgressByPatient(patientId);
        return ResponseEntity.ok(ApiResponse.ok(logs));
    }
    
    @GetMapping("/analyze/{treatmentId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> analyzeProgress(
            @PathVariable String treatmentId,
            @RequestParam(defaultValue = "30") int jours) {
        
        Map<String, Object> analysis = progressService.analyzeProgress(treatmentId, jours);
        return ResponseEntity.ok(ApiResponse.ok(analysis));
    }
}
