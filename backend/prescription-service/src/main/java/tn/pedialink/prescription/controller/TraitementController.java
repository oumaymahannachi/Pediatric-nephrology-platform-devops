package tn.pedialink.prescription.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tn.pedialink.prescription.dto.ApiResponse;
import tn.pedialink.prescription.dto.traitement.TraitementCreateRequest;
import tn.pedialink.prescription.dto.traitement.TraitementResponse;
import tn.pedialink.prescription.service.TraitementChroniqueService;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/traitements")
@RequiredArgsConstructor
public class TraitementController {

    private final TraitementChroniqueService traitementService;

    @PostMapping
    public ResponseEntity<ApiResponse<TraitementResponse>> creerTraitement(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TraitementCreateRequest request) {

        String medecinId = userDetails != null ? userDetails.getUsername() : "doctor-test";
        TraitementResponse response = traitementService.creerTraitement(medecinId, request);
        return ResponseEntity.ok(ApiResponse.created(response));
    }

    @GetMapping("/patient/{patientId}/actifs")
    public ResponseEntity<ApiResponse<List<TraitementResponse>>> getTraitementsActifs(
            @PathVariable String patientId) {

        List<TraitementResponse> traitements = traitementService.getTraitementsActifsPatient(patientId);
        return ResponseEntity.ok(ApiResponse.ok(traitements));
    }

    @PostMapping("/{id}/renouveler")
    public ResponseEntity<ApiResponse<TraitementResponse>> enregistrerRenouvellement(
            @PathVariable String id,
            @RequestParam String prescriptionId,
            @RequestParam String pharmacienId) {

        TraitementResponse response = traitementService.enregistrerRenouvellement(
                id, prescriptionId, pharmacienId);
        return ResponseEntity.ok(ApiResponse.ok("Renouvellement enregistré", response));
    }

    @PostMapping("/{id}/observance")
    public ResponseEntity<ApiResponse<Void>> enregistrerObservance(
            @PathVariable String id,
            @RequestParam LocalDate date,
            @RequestParam Boolean pris,
            @RequestParam(required = false) Integer heurePrise,
            @RequestParam(required = false) String commentaire) {

        traitementService.enregistrerObservance(id, date, pris, heurePrise, commentaire);
        return ResponseEntity.ok(ApiResponse.ok("Observance enregistrée", null));
    }

    @PostMapping("/{id}/bilans/{type}/realise")
    public ResponseEntity<ApiResponse<Void>> marquerBilanRealise(
            @PathVariable String id,
            @PathVariable String type,
            @RequestParam LocalDate dateRealisation,
            @RequestParam(required = false) String resultat) {

        traitementService.marquerBilanRealise(id, type, dateRealisation, resultat);
        return ResponseEntity.ok(ApiResponse.ok("Bilan marqué comme réalisé", null));
    }

    @GetMapping("/moi/actifs")
    public ResponseEntity<ApiResponse<List<TraitementResponse>>> getMesTraitementsActifs(
            @AuthenticationPrincipal UserDetails userDetails) {

        String patientId = userDetails != null ? userDetails.getUsername() : "patient-test";
        List<TraitementResponse> traitements = traitementService.getTraitementsActifsPatient(patientId);
        return ResponseEntity.ok(ApiResponse.ok(traitements));
    }
}