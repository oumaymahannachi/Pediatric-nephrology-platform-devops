package tn.pedialink.dossiermedical.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO compatible avec l'ancienne API appointments
 * Mappé vers ConsultationDto en interne
 */
@Data
public class AppointmentDto {
    private String id;       // ID de la consultation
    @NotBlank
    private String childId;  // Mappé vers patientId
    private String childName; // Nom de l'enfant (optionnel)
    @NotBlank
    private String doctorId; // Mappé vers medecinId
    private String parentId; // ID du parent
    @NotBlank
    private String dateTime; // Mappé vers dateRendezVous (format ISO)
    private String reason;   // Mappé vers motifConsultation
    private String notes;    // Mappé vers observationsCliniques ou compteRendu
    private String status;   // Statut: PENDING, ACCEPTED, REFUSED, COMPLETED, CANCELLED
}
