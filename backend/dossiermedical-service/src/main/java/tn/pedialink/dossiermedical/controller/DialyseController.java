package tn.pedialink.dossiermedical.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.pedialink.dossiermedical.dto.ApiResponse;
import tn.pedialink.dossiermedical.dto.DialyseDto;
import tn.pedialink.dossiermedical.model.dialyse.Dialyse;
import tn.pedialink.dossiermedical.service.DialyseService;
import java.util.List;

@RestController
@RequestMapping("/api/dialyses")
@RequiredArgsConstructor
public class DialyseController {
    private final DialyseService dialyseService;

    @PostMapping
    public ResponseEntity<ApiResponse> createDialyse(@Valid @RequestBody DialyseDto dto) {
        Dialyse dialyse = dialyseService.createDialyse(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse(true, "Séance de dialyse créée avec succès", dialyse));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateDialyse(@PathVariable String id, @Valid @RequestBody DialyseDto dto) {
        Dialyse dialyse = dialyseService.updateDialyse(id, dto);
        return ResponseEntity.ok(new ApiResponse(true, "Séance de dialyse mise à jour", dialyse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getDialyseById(@PathVariable String id) {
        Dialyse dialyse = dialyseService.getDialyseById(id);
        return ResponseEntity.ok(new ApiResponse(true, "Séance de dialyse trouvée", dialyse));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse> getDialysesByPatient(@PathVariable String patientId) {
        List<Dialyse> dialyses = dialyseService.getDialysesByPatient(patientId);
        return ResponseEntity.ok(new ApiResponse(true, "Séances de dialyse du patient", dialyses));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllDialyses() {
        List<Dialyse> dialyses = dialyseService.getAllDialyses();
        return ResponseEntity.ok(new ApiResponse(true, "Liste des séances de dialyse", dialyses));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteDialyse(@PathVariable String id) {
        dialyseService.deleteDialyse(id);
        return ResponseEntity.ok(new ApiResponse(true, "Séance de dialyse supprimée", null));
    }
}
