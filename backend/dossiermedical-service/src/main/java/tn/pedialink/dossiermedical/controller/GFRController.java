package tn.pedialink.dossiermedical.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.pedialink.dossiermedical.dto.GFRCalculationDto;
import tn.pedialink.dossiermedical.model.kidney.GFRCalculation;
import tn.pedialink.dossiermedical.service.GFRCalculationService;

import java.util.List;

@RestController
@RequestMapping("/api/gfr")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class GFRController {
    
    private final GFRCalculationService gfrService;
    
    @PostMapping("/calculate")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<GFRCalculation> calculateGFR(@Valid @RequestBody GFRCalculationDto dto) {
        GFRCalculation result = gfrService.calculateGFR(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
    
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PARENT')")
    public ResponseEntity<List<GFRCalculation>> getPatientHistory(@PathVariable String patientId) {
        List<GFRCalculation> history = gfrService.getPatientGFRHistory(patientId);
        return ResponseEntity.ok(history);
    }
    
    @GetMapping("/patient/{patientId}/latest")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PARENT')")
    public ResponseEntity<GFRCalculation> getLatestGFR(@PathVariable String patientId) {
        GFRCalculation latest = gfrService.getLatestGFR(patientId);
        if (latest == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(latest);
    }
    
    @GetMapping("/doctor/{medecinId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<GFRCalculation>> getDoctorCalculations(@PathVariable String medecinId) {
        List<GFRCalculation> calculations = gfrService.getDoctorGFRCalculations(medecinId);
        return ResponseEntity.ok(calculations);
    }
}
