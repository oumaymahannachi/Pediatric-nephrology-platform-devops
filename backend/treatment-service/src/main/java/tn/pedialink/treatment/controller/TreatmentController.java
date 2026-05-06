package tn.pedialink.treatment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.pedialink.treatment.dto.ApiResponse;
import tn.pedialink.treatment.dto.TreatmentRequest;
import tn.pedialink.treatment.entity.Treatment;
import tn.pedialink.treatment.service.TreatmentService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/traitements")
@RequiredArgsConstructor
public class TreatmentController {
    
    private final TreatmentService treatmentService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<Treatment>> createTreatment(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody TreatmentRequest request) {
        String medecinId = userId != null ? userId : "doctor-test";
        Treatment treatment = treatmentService.createTreatment(medecinId, request);
        return ResponseEntity.ok(ApiResponse.ok("Traitement créé avec succès", treatment));
    }
    
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<List<Treatment>>> getTreatmentsByPatient(@PathVariable String patientId) {
        List<Treatment> treatments = treatmentService.getTreatmentsByPatient(patientId);
        return ResponseEntity.ok(ApiResponse.ok(treatments));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Treatment>> getTreatment(@PathVariable String id) {
        Treatment treatment = treatmentService.getTreatment(id);
        return ResponseEntity.ok(ApiResponse.ok(treatment));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Treatment>> updateTreatment(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @PathVariable String id,
            @RequestBody TreatmentRequest request) {
        String medecinId = userId != null ? userId : "doctor-test";
        Treatment treatment = treatmentService.updateTreatment(id, medecinId, request);
        return ResponseEntity.ok(ApiResponse.ok("Traitement modifié avec succès", treatment));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTreatment(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @PathVariable String id) {
        String medecinId = userId != null ? userId : "doctor-test";
        treatmentService.deleteTreatment(id, medecinId);
        return ResponseEntity.ok(ApiResponse.ok("Traitement supprimé avec succès", null));
    }
}
