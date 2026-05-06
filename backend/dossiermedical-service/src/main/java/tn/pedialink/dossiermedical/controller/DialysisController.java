package tn.pedialink.dossiermedical.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.pedialink.dossiermedical.dto.ApiResponse;
import tn.pedialink.dossiermedical.dto.DialysisPrescriptionDto;
import tn.pedialink.dossiermedical.dto.DialysisSessionDto;
import tn.pedialink.dossiermedical.model.dialyse.DialysisPrescription;
import tn.pedialink.dossiermedical.model.dialyse.DialysisSession;
import tn.pedialink.dossiermedical.service.DialysisPrescriptionService;
import tn.pedialink.dossiermedical.service.DialysisSessionService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dialyses")
@RequiredArgsConstructor
public class DialysisController {
    
    private final DialysisPrescriptionService prescriptionService;
    private final DialysisSessionService sessionService;

    // ========== PRESCRIPTION ENDPOINTS ==========
    
    @PostMapping("/prescriptions")
    public ResponseEntity<DialysisPrescription> createPrescription(
            @Valid @RequestBody DialysisPrescriptionDto dto) {
        DialysisPrescription prescription = prescriptionService.createPrescription(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(prescription);
    }

    @PutMapping("/prescriptions/{id}")
    public ResponseEntity<DialysisPrescription> updatePrescription(
            @PathVariable String id,
            @Valid @RequestBody DialysisPrescriptionDto dto) {
        DialysisPrescription prescription = prescriptionService.updatePrescription(id, dto);
        return ResponseEntity.ok(prescription);
    }

    @DeleteMapping("/prescriptions/{id}")
    public ResponseEntity<ApiResponse> deactivatePrescription(@PathVariable String id) {
        prescriptionService.deactivatePrescription(id);
        return ResponseEntity.ok(new ApiResponse(true, "Prescription deactivated", null));
    }

    @GetMapping("/prescriptions/{id}")
    public ResponseEntity<DialysisPrescription> getPrescription(@PathVariable String id) {
        DialysisPrescription prescription = prescriptionService.getPrescriptionById(id);
        return ResponseEntity.ok(prescription);
    }

    @GetMapping("/prescriptions/patient/{patientId}")
    public ResponseEntity<List<DialysisPrescription>> getPrescriptionsByPatient(
            @PathVariable String patientId) {
        List<DialysisPrescription> prescriptions = prescriptionService.getPrescriptionsByPatient(patientId);
        return ResponseEntity.ok(prescriptions);
    }

    @GetMapping("/prescriptions/patient/{patientId}/active")
    public ResponseEntity<List<DialysisPrescription>> getActivePrescriptionsByPatient(
            @PathVariable String patientId) {
        List<DialysisPrescription> prescriptions = prescriptionService.getActivePrescriptionsByPatient(patientId);
        return ResponseEntity.ok(prescriptions);
    }

    @GetMapping("/prescriptions/doctor/{medecinId}")
    public ResponseEntity<List<DialysisPrescription>> getPrescriptionsByDoctor(
            @PathVariable String medecinId) {
        List<DialysisPrescription> prescriptions = prescriptionService.getPrescriptionsByDoctor(medecinId);
        return ResponseEntity.ok(prescriptions);
    }

    // ========== SESSION ENDPOINTS ==========
    
    @PostMapping("/sessions")
    public ResponseEntity<DialysisSession> scheduleSession(
            @Valid @RequestBody DialysisSessionDto dto) {
        DialysisSession session = sessionService.scheduleSession(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(session);
    }

    @PostMapping("/sessions/{id}/start")
    public ResponseEntity<DialysisSession> startSession(@PathVariable String id) {
        DialysisSession session = sessionService.startSession(id);
        return ResponseEntity.ok(session);
    }

    @PostMapping("/sessions/{id}/complete")
    public ResponseEntity<DialysisSession> completeSession(
            @PathVariable String id,
            @Valid @RequestBody DialysisSessionDto dto) {
        DialysisSession session = sessionService.completeSession(id, dto);
        return ResponseEntity.ok(session);
    }

    @PostMapping("/sessions/{id}/cancel")
    public ResponseEntity<DialysisSession> cancelSession(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "Cancelled");
        DialysisSession session = sessionService.cancelSession(id, reason);
        return ResponseEntity.ok(session);
    }

    @PostMapping("/sessions/{id}/reschedule")
    public ResponseEntity<DialysisSession> rescheduleSession(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        LocalDateTime newDate = LocalDateTime.parse(body.get("scheduledDate"));
        DialysisSession session = sessionService.rescheduleSession(id, newDate);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/sessions/{id}")
    public ResponseEntity<DialysisSession> getSession(@PathVariable String id) {
        DialysisSession session = sessionService.getSessionById(id);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/sessions/patient/{patientId}")
    public ResponseEntity<List<DialysisSession>> getSessionsByPatient(
            @PathVariable String patientId) {
        List<DialysisSession> sessions = sessionService.getSessionsByPatient(patientId);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/sessions/prescription/{prescriptionId}")
    public ResponseEntity<List<DialysisSession>> getSessionsByPrescription(
            @PathVariable String prescriptionId) {
        List<DialysisSession> sessions = sessionService.getSessionsByPrescription(prescriptionId);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/sessions/doctor/{medecinId}")
    public ResponseEntity<List<DialysisSession>> getSessionsByDoctor(
            @PathVariable String medecinId) {
        List<DialysisSession> sessions = sessionService.getSessionsByDoctor(medecinId);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/sessions/range")
    public ResponseEntity<List<DialysisSession>> getSessionsByDateRange(
            @RequestParam String start,
            @RequestParam String end) {
        LocalDateTime startDate = LocalDateTime.parse(start);
        LocalDateTime endDate = LocalDateTime.parse(end);
        List<DialysisSession> sessions = sessionService.getSessionsByDateRange(startDate, endDate);
        return ResponseEntity.ok(sessions);
    }
}
