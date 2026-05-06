package tn.pedialink.dossiermedical.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.pedialink.dossiermedical.dto.ApiResponse;
import tn.pedialink.dossiermedical.dto.AppointmentDto;
import tn.pedialink.dossiermedical.dto.ConsultationDto;
import tn.pedialink.dossiermedical.model.consultation.Consultation;
import tn.pedialink.dossiermedical.security.JwtUtil;
import tn.pedialink.dossiermedical.service.AppointmentMappingService;
import tn.pedialink.dossiermedical.service.ConsultationService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Controller pour gérer les appointments (consultations)
 * Compatible avec l'ancienne API du treatment-monitoring-service
 */
@RestController
@RequiredArgsConstructor
public class AppointmentController {
    
    private final ConsultationService consultationService;
    private final AppointmentMappingService mappingService;
    private final JwtUtil jwtUtil;
    
    /**
     * Extrait l'userId du token JWT dans le header Authorization
     */
    private String extractUserIdFromToken(String authHeader) {
        System.out.println("=== DEBUG: Authorization Header = " + authHeader);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            System.out.println("=== DEBUG: Token extrait = " + token.substring(0, Math.min(20, token.length())) + "...");
            try {
                String userId = jwtUtil.getUserIdFromToken(token);
                System.out.println("=== DEBUG: UserId extrait = " + userId);
                return userId;
            } catch (Exception e) {
                System.err.println("=== ERROR: Erreur extraction userId: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }
        System.out.println("=== DEBUG: Pas de Bearer token trouvé");
        return null;
    }

    // ========== PARENT ENDPOINTS ==========
    
    @GetMapping("/api/parent/appointments")
    public ResponseEntity<List<AppointmentDto>> getParentAppointments(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        System.out.println("=== DEBUG: Récupération appointments parent");
        String parentId = extractUserIdFromToken(authHeader);
        System.out.println("=== DEBUG: ParentId: " + parentId);
        
        if (parentId == null) {
            return ResponseEntity.ok(List.of());
        }
        
        // Chercher par parentId
        List<Consultation> consultations = consultationService.getConsultationsByParent(parentId);
        System.out.println("=== DEBUG: Nombre de consultations trouvées: " + consultations.size());
        
        // Convertir en AppointmentDto
        List<AppointmentDto> appointments = consultations.stream()
                .map(mappingService::consultationToAppointment)
                .toList();
        
        return ResponseEntity.ok(appointments);
    }

    @PostMapping("/api/parent/appointments")
    public ResponseEntity<AppointmentDto> createAppointment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody AppointmentDto appointmentDto) {
        System.out.println("=== DEBUG: Création appointment - Authorization header présent: " + (authHeader != null));
        String parentId = extractUserIdFromToken(authHeader);
        System.out.println("=== DEBUG: ParentId extrait: " + parentId);
        
        // Mapper AppointmentDto vers ConsultationDto
        ConsultationDto consultationDto = mappingService.appointmentToConsultation(appointmentDto);
        
        // Ajouter le parentId avant de créer
        consultationDto.setParentId(parentId);
        System.out.println("=== DEBUG: ConsultationDto avec parentId: " + consultationDto.getParentId());
        
        Consultation consultation = consultationService.createConsultationRequest(consultationDto);
        System.out.println("=== DEBUG: Consultation créée avec parentId: " + consultation.getParentId());
        
        // Convertir en AppointmentDto pour la réponse
        AppointmentDto response = mappingService.consultationToAppointment(consultation);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/api/parent/appointments/{id}")
    public ResponseEntity<ApiResponse> cancelAppointment(@PathVariable String id) {
        consultationService.deleteConsultation(id);
        return ResponseEntity.ok(new ApiResponse(true, "Rendez-vous annulé", null));
    }

    // ========== DOCTOR ENDPOINTS ==========
    
    @GetMapping("/api/doctor/appointments")
    public ResponseEntity<List<AppointmentDto>> getDoctorAppointments(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String doctorId = extractUserIdFromToken(authHeader);
        if (doctorId == null) {
            return ResponseEntity.ok(List.of());
        }
        List<Consultation> consultations = consultationService.getConsultationsByMedecin(doctorId);
        
        // Convertir en AppointmentDto
        List<AppointmentDto> appointments = consultations.stream()
                .map(mappingService::consultationToAppointment)
                .toList();
        
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/api/doctor/appointments/pending")
    public ResponseEntity<List<AppointmentDto>> getPendingAppointments(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String doctorId = extractUserIdFromToken(authHeader);
        if (doctorId == null) {
            return ResponseEntity.ok(List.of());
        }
        List<Consultation> consultations = consultationService.getPendingConsultationsForMedecin(doctorId);
        
        // Convertir en AppointmentDto
        List<AppointmentDto> appointments = consultations.stream()
                .map(mappingService::consultationToAppointment)
                .toList();
        
        return ResponseEntity.ok(appointments);
    }

    @PostMapping("/api/doctor/appointments/{id}/accept")
    public ResponseEntity<AppointmentDto> acceptAppointment(@PathVariable String id) {
        Consultation consultation = consultationService.acceptConsultation(id);
        
        // TODO: Automatically assign doctor to child in treatment-monitoring-service
        // This would require calling treatment-monitoring-service REST API
        // For now, parents must manually assign doctors to children
        
        AppointmentDto response = mappingService.consultationToAppointment(consultation);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/api/doctor/appointments/{id}/accept")
    public ResponseEntity<AppointmentDto> acceptAppointmentPut(@PathVariable String id) {
        return acceptAppointment(id);
    }
    
    @PostMapping("/api/doctor/appointments/{id}/refuse")
    @PutMapping("/api/doctor/appointments/{id}/refuse")
    public ResponseEntity<AppointmentDto> refuseAppointment(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, Object> body) {
        LocalDateTime dateProposee = null;
        String raisonRefus = "Refusé par le médecin";
        
        if (body != null) {
            if (body.containsKey("proposedDate")) {
                dateProposee = LocalDateTime.parse(body.get("proposedDate").toString(), 
                    DateTimeFormatter.ISO_DATE_TIME);
            }
            if (body.containsKey("reason")) {
                raisonRefus = body.get("reason").toString();
            }
        }
        
        Consultation consultation = consultationService.refuseConsultation(id, dateProposee, raisonRefus);
        AppointmentDto response = mappingService.consultationToAppointment(consultation);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/api/doctor/appointments/{id}/reschedule")
    @PutMapping("/api/doctor/appointments/{id}/reschedule")
    public ResponseEntity<AppointmentDto> rescheduleAppointment(
            @PathVariable String id,
            @RequestBody Map<String, Object> body) {
        LocalDateTime newDate = null;
        String reason = "Reprogrammé par le médecin";
        
        if (body != null && body.containsKey("newDate")) {
            newDate = LocalDateTime.parse(body.get("newDate").toString(), 
                DateTimeFormatter.ISO_DATE_TIME);
        }
        if (body != null && body.containsKey("reason")) {
            reason = body.get("reason").toString();
        }
        
        if (newDate == null) {
            throw new RuntimeException("La nouvelle date est requise");
        }
        
        Consultation consultation = consultationService.refuseConsultation(id, newDate, reason);
        AppointmentDto response = mappingService.consultationToAppointment(consultation);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/doctor/appointments/{id}/complete")
    public ResponseEntity<AppointmentDto> completeAppointment(
            @PathVariable String id,
            @RequestBody Map<String, String> recordData) {
        System.out.println("=== DEBUG: Complete appointment " + id);
        System.out.println("=== DEBUG: Record data: " + recordData);
        
        ConsultationDto dto = new ConsultationDto();
        dto.setObservationsCliniques(recordData.get("observationsCliniques"));
        dto.setDiagnostic(recordData.get("diagnostic"));
        dto.setRecommandations(recordData.get("recommandations"));
        dto.setCompteRendu(recordData.get("compteRendu"));
        
        Consultation consultation = consultationService.completeConsultation(id, dto);
        AppointmentDto response = mappingService.consultationToAppointment(consultation);
        return ResponseEntity.ok(response);
    }
}
