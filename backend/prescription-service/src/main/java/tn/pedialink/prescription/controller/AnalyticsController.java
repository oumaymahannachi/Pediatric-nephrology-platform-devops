package tn.pedialink.prescription.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tn.pedialink.prescription.dto.ApiResponse;
import tn.pedialink.prescription.service.AnalyticsService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    
    private final AnalyticsService analyticsService;
    
    @GetMapping("/medecin/{medecinId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistiquesMedecin(
            @PathVariable String medecinId,
            @RequestParam(defaultValue = "30") int jours) {
        
        Map<String, Object> stats = analyticsService.getStatistiquesMedecin(medecinId, jours);
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }
    
    @GetMapping("/medicaments/top")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTopMedicaments(
            @RequestParam(defaultValue = "30") int jours,
            @RequestParam(defaultValue = "10") int limit) {
        
        Map<String, Object> top = analyticsService.getTopMedicaments(jours, limit);
        return ResponseEntity.ok(ApiResponse.ok(top));
    }
    
    @GetMapping("/interactions/frequentes")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getInteractionsFrequentes() {
        Map<String, Object> interactions = analyticsService.getInteractionsFrequentes();
        return ResponseEntity.ok(ApiResponse.ok(interactions));
    }
    
    @GetMapping("/patient/{patientId}/historique")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHistoriquePatient(@PathVariable String patientId) {
        Map<String, Object> historique = analyticsService.getHistoriquePatient(patientId);
        return ResponseEntity.ok(ApiResponse.ok(historique));
    }
}
