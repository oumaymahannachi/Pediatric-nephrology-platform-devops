package tn.pedialink.prescription.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tn.pedialink.prescription.dto.ApiResponse;
import tn.pedialink.prescription.dto.prescription.PrescriptionCreateRequest;
import tn.pedialink.prescription.dto.prescription.PrescriptionResponse;
import tn.pedialink.prescription.service.PrescriptionService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @PostMapping
    public ResponseEntity<ApiResponse<PrescriptionResponse>> creerPrescription(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PrescriptionCreateRequest request) {

        String medecinId = userDetails != null ? userDetails.getUsername() : "doctor-test";
        PrescriptionResponse response = prescriptionService.creerPrescription(medecinId, request);
        return ResponseEntity.ok(ApiResponse.created(response));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<List<PrescriptionResponse>>> getPrescriptionsPatient(
            @PathVariable String patientId) {

        List<PrescriptionResponse> prescriptions = prescriptionService.getPrescriptionsPatient(patientId);
        return ResponseEntity.ok(ApiResponse.ok(prescriptions));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> getPrescription(@PathVariable String id) {
        PrescriptionResponse prescription = prescriptionService.getPrescription(id);
        return ResponseEntity.ok(ApiResponse.ok(prescription));
    }

    @PostMapping("/{id}/renouveler")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> renouvelerPrescription(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id) {

        String medecinId = userDetails != null ? userDetails.getUsername() : "doctor-test";
        PrescriptionResponse response = prescriptionService.renouvelerPrescription(id, medecinId);
        return ResponseEntity.ok(ApiResponse.ok("Prescription renouvelée avec succès", response));
    }

    @GetMapping("/moi")
    public ResponseEntity<ApiResponse<List<PrescriptionResponse>>> getMesPrescriptions(
            @AuthenticationPrincipal UserDetails userDetails) {

        String patientId = userDetails != null ? userDetails.getUsername() : "patient-test";
        List<PrescriptionResponse> prescriptions = prescriptionService.getPrescriptionsPatient(patientId);
        return ResponseEntity.ok(ApiResponse.ok(prescriptions));
    }


    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> modifierPrescription(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id,
            @Valid @RequestBody PrescriptionCreateRequest request) {

        String medecinId = userDetails != null ? userDetails.getUsername() : "doctor-test";
        PrescriptionResponse response = prescriptionService.modifierPrescription(id, medecinId, request);
        return ResponseEntity.ok(ApiResponse.ok("Prescription modifiée avec succès", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> supprimerPrescription(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id) {

        String medecinId = userDetails != null ? userDetails.getUsername() : "doctor-test";
        prescriptionService.supprimerPrescription(id, medecinId);
        return ResponseEntity.ok(ApiResponse.ok("Prescription supprimée avec succès", null));
    }

}