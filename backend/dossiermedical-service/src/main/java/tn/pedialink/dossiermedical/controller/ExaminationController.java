package tn.pedialink.dossiermedical.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.pedialink.dossiermedical.dto.BloodTestDto;
import tn.pedialink.dossiermedical.dto.LabResultDto;
import tn.pedialink.dossiermedical.dto.MedicalImagingDto;
import tn.pedialink.dossiermedical.model.examen.BloodTest;
import tn.pedialink.dossiermedical.model.examen.LabResult;
import tn.pedialink.dossiermedical.model.examen.MedicalImaging;
import tn.pedialink.dossiermedical.service.BloodTestService;
import tn.pedialink.dossiermedical.service.LabResultService;
import tn.pedialink.dossiermedical.service.MedicalImagingService;

import java.util.List;

@RestController
@RequestMapping("/api/examinations")
@RequiredArgsConstructor
public class ExaminationController {
    private final BloodTestService bloodTestService;
    private final LabResultService labResultService;
    private final MedicalImagingService medicalImagingService;

    // ===== Blood Tests =====

    @PostMapping("/blood-tests")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<BloodTest> createBloodTest(@Valid @RequestBody BloodTestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bloodTestService.createBloodTest(dto));
    }

    @GetMapping("/blood-tests/patient/{patientId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PARENT')")
    public ResponseEntity<List<BloodTest>> getBloodTestsByPatient(@PathVariable String patientId) {
        return ResponseEntity.ok(bloodTestService.getBloodTestsByPatient(patientId));
    }

    @GetMapping("/blood-tests/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PARENT')")
    public ResponseEntity<BloodTest> getBloodTest(@PathVariable String id) {
        return ResponseEntity.ok(bloodTestService.getBloodTestById(id));
    }

    @PutMapping("/blood-tests/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<BloodTest> updateBloodTest(@PathVariable String id, @Valid @RequestBody BloodTestDto dto) {
        return ResponseEntity.ok(bloodTestService.updateBloodTest(id, dto));
    }

    @DeleteMapping("/blood-tests/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> deleteBloodTest(@PathVariable String id) {
        bloodTestService.deleteBloodTest(id);
        return ResponseEntity.noContent().build();
    }

    // ===== Lab Results =====

    @PostMapping("/lab-results")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<LabResult> createLabResult(@Valid @RequestBody LabResultDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(labResultService.createLabResult(dto));
    }

    @GetMapping("/lab-results/patient/{patientId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PARENT')")
    public ResponseEntity<List<LabResult>> getLabResultsByPatient(@PathVariable String patientId) {
        return ResponseEntity.ok(labResultService.getLabResultsByPatient(patientId));
    }

    @GetMapping("/lab-results/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PARENT')")
    public ResponseEntity<LabResult> getLabResult(@PathVariable String id) {
        return ResponseEntity.ok(labResultService.getLabResultById(id));
    }

    @PutMapping("/lab-results/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<LabResult> updateLabResult(@PathVariable String id, @Valid @RequestBody LabResultDto dto) {
        return ResponseEntity.ok(labResultService.updateLabResult(id, dto));
    }

    @DeleteMapping("/lab-results/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> deleteLabResult(@PathVariable String id) {
        labResultService.deleteLabResult(id);
        return ResponseEntity.noContent().build();
    }

    // ===== Medical Imaging =====

    @PostMapping("/imaging")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<MedicalImaging> createMedicalImaging(@Valid @RequestBody MedicalImagingDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicalImagingService.createMedicalImaging(dto));
    }

    @GetMapping("/imaging/patient/{patientId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PARENT')")
    public ResponseEntity<List<MedicalImaging>> getMedicalImagingByPatient(@PathVariable String patientId) {
        return ResponseEntity.ok(medicalImagingService.getMedicalImagingByPatient(patientId));
    }

    @GetMapping("/imaging/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PARENT')")
    public ResponseEntity<MedicalImaging> getMedicalImaging(@PathVariable String id) {
        return ResponseEntity.ok(medicalImagingService.getMedicalImagingById(id));
    }

    @PutMapping("/imaging/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<MedicalImaging> updateMedicalImaging(@PathVariable String id, @Valid @RequestBody MedicalImagingDto dto) {
        return ResponseEntity.ok(medicalImagingService.updateMedicalImaging(id, dto));
    }

    @DeleteMapping("/imaging/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> deleteMedicalImaging(@PathVariable String id) {
        medicalImagingService.deleteMedicalImaging(id);
        return ResponseEntity.noContent().build();
    }
}
