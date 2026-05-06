package tn.pedialink.dossiermedical.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.pedialink.dossiermedical.dto.ApiResponse;
import tn.pedialink.dossiermedical.dto.ConsultationDto;
import tn.pedialink.dossiermedical.model.consultation.Consultation;
import tn.pedialink.dossiermedical.service.ConsultationService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consultations")
@RequiredArgsConstructor
public class ConsultationController {
    private final ConsultationService consultationService;

    // Parent crée une demande de consultation
    @PostMapping("/request")
    public ResponseEntity<ApiResponse> createConsultationRequest(@Valid @RequestBody ConsultationDto dto) {
        Consultation consultation = consultationService.createConsultationRequest(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse(true, "Demande de consultation créée avec succès", consultation));
    }

    // Médecin accepte la consultation
    @PostMapping("/{id}/accept")
    public ResponseEntity<ApiResponse> acceptConsultation(@PathVariable String id) {
        Consultation consultation = consultationService.acceptConsultation(id);
        return ResponseEntity.ok(new ApiResponse(true, "Consultation acceptée", consultation));
    }

    // Médecin refuse et propose une autre date
    @PostMapping("/{id}/refuse")
    public ResponseEntity<ApiResponse> refuseConsultation(
            @PathVariable String id,
            @RequestBody Map<String, Object> payload) {
        
        LocalDateTime dateProposee = payload.get("dateProposee") != null 
            ? LocalDateTime.parse((String) payload.get("dateProposee"))
            : null;
        String raisonRefus = (String) payload.get("raisonRefus");
        
        Consultation consultation = consultationService.refuseConsultation(id, dateProposee, raisonRefus);
        return ResponseEntity.ok(new ApiResponse(true, "Consultation refusée", consultation));
    }

    // Parent accepte la date proposée
    @PostMapping("/{id}/accept-proposed")
    public ResponseEntity<ApiResponse> acceptProposedDate(@PathVariable String id) {
        Consultation consultation = consultationService.acceptProposedDate(id);
        return ResponseEntity.ok(new ApiResponse(true, "Date proposée acceptée", consultation));
    }

    // Médecin complète la consultation après le rendez-vous
    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse> completeConsultation(
            @PathVariable String id,
            @Valid @RequestBody ConsultationDto dto) {
        Consultation consultation = consultationService.completeConsultation(id, dto);
        return ResponseEntity.ok(new ApiResponse(true, "Consultation complétée", consultation));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getConsultationById(@PathVariable String id) {
        Consultation consultation = consultationService.getConsultationById(id);
        return ResponseEntity.ok(new ApiResponse(true, "Consultation trouvée", consultation));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse> getConsultationsByPatient(@PathVariable String patientId) {
        List<Consultation> consultations = consultationService.getConsultationsByPatient(patientId);
        return ResponseEntity.ok(new ApiResponse(true, "Consultations du patient", consultations));
    }

    @GetMapping("/medecin/{medecinId}")
    public ResponseEntity<ApiResponse> getConsultationsByMedecin(@PathVariable String medecinId) {
        List<Consultation> consultations = consultationService.getConsultationsByMedecin(medecinId);
        return ResponseEntity.ok(new ApiResponse(true, "Consultations du médecin", consultations));
    }

    @GetMapping("/medecin/{medecinId}/pending")
    public ResponseEntity<ApiResponse> getPendingConsultations(@PathVariable String medecinId) {
        List<Consultation> consultations = consultationService.getPendingConsultationsForMedecin(medecinId);
        return ResponseEntity.ok(new ApiResponse(true, "Consultations en attente", consultations));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllConsultations() {
        List<Consultation> consultations = consultationService.getAllConsultations();
        return ResponseEntity.ok(new ApiResponse(true, "Liste des consultations", consultations));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteConsultation(@PathVariable String id) {
        consultationService.deleteConsultation(id);
        return ResponseEntity.ok(new ApiResponse(true, "Consultation supprimée", null));
    }
}
