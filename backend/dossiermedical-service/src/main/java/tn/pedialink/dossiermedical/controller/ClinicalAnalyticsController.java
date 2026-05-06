package tn.pedialink.dossiermedical.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.pedialink.dossiermedical.model.analytics.ClinicalAlert;
import tn.pedialink.dossiermedical.model.analytics.DialysisRiskScore;
import tn.pedialink.dossiermedical.service.ClinicalAlertService;
import tn.pedialink.dossiermedical.service.DialysisRiskService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class ClinicalAnalyticsController {

    private final ClinicalAlertService alertService;
    private final DialysisRiskService riskService;

    // ===== Alertes cliniques =====

    @GetMapping("/alerts/patient/{patientId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PARENT')")
    public ResponseEntity<List<ClinicalAlert>> getPatientAlerts(@PathVariable String patientId) {
        return ResponseEntity.ok(alertService.getPatientAlerts(patientId));
    }

    @GetMapping("/alerts/doctor/{medecinId}/unacknowledged")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<ClinicalAlert>> getUnacknowledgedAlerts(@PathVariable String medecinId) {
        return ResponseEntity.ok(alertService.getUnacknowledgedAlertsForDoctor(medecinId));
    }

    @PutMapping("/alerts/{alertId}/acknowledge")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ClinicalAlert> acknowledgeAlert(
            @PathVariable String alertId,
            @RequestBody Map<String, String> body) {
        String medecinId = body.get("medecinId");
        return ResponseEntity.ok(alertService.acknowledgeAlert(alertId, medecinId));
    }

    // ===== Score de risque dialyse =====

    @GetMapping("/dialysis-risk/{patientId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PARENT')")
    public ResponseEntity<DialysisRiskScore> getDialysisRisk(@PathVariable String patientId) {
        return ResponseEntity.ok(riskService.calculateRisk(patientId));
    }
}
