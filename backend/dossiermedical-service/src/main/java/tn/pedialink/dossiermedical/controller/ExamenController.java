package tn.pedialink.dossiermedical.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.pedialink.dossiermedical.dto.ApiResponse;
import tn.pedialink.dossiermedical.dto.ExamenDto;
import tn.pedialink.dossiermedical.model.examen.Examen;
import tn.pedialink.dossiermedical.model.examen.TypeExamen;
import tn.pedialink.dossiermedical.service.ExamenService;
import java.util.List;

@RestController
@RequestMapping("/api/examens")
@RequiredArgsConstructor
public class ExamenController {
    private final ExamenService examenService;

    @PostMapping
    public ResponseEntity<ApiResponse> createExamen(@Valid @RequestBody ExamenDto dto) {
        Examen examen = examenService.createExamen(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse(true, "Examen créé avec succès", examen));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateExamen(@PathVariable String id, @Valid @RequestBody ExamenDto dto) {
        Examen examen = examenService.updateExamen(id, dto);
        return ResponseEntity.ok(new ApiResponse(true, "Examen mis à jour", examen));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getExamenById(@PathVariable String id) {
        Examen examen = examenService.getExamenById(id);
        return ResponseEntity.ok(new ApiResponse(true, "Examen trouvé", examen));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse> getExamensByPatient(@PathVariable String patientId) {
        List<Examen> examens = examenService.getExamensByPatient(patientId);
        return ResponseEntity.ok(new ApiResponse(true, "Examens du patient", examens));
    }

    @GetMapping("/patient/{patientId}/type/{typeExamen}")
    public ResponseEntity<ApiResponse> getExamensByPatientAndType(
            @PathVariable String patientId, 
            @PathVariable TypeExamen typeExamen) {
        List<Examen> examens = examenService.getExamensByPatientAndType(patientId, typeExamen);
        return ResponseEntity.ok(new ApiResponse(true, "Examens filtrés", examens));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllExamens() {
        List<Examen> examens = examenService.getAllExamens();
        return ResponseEntity.ok(new ApiResponse(true, "Liste des examens", examens));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteExamen(@PathVariable String id) {
        examenService.deleteExamen(id);
        return ResponseEntity.ok(new ApiResponse(true, "Examen supprimé", null));
    }
}
