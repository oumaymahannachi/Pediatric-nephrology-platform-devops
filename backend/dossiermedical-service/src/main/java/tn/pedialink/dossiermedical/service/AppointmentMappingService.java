package tn.pedialink.dossiermedical.service;

import org.springframework.stereotype.Service;
import tn.pedialink.dossiermedical.dto.AppointmentDto;
import tn.pedialink.dossiermedical.dto.ConsultationDto;
import tn.pedialink.dossiermedical.model.consultation.StatutConsultation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service pour mapper les anciens DTOs Appointment vers les nouveaux DTOs Consultation
 */
@Service
public class AppointmentMappingService {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    public ConsultationDto appointmentToConsultation(AppointmentDto appointmentDto) {
        ConsultationDto consultationDto = new ConsultationDto();
        
        // Mapping des champs
        consultationDto.setPatientId(appointmentDto.getChildId());
        consultationDto.setPatientName(appointmentDto.getChildName()); // Ajouter le nom
        consultationDto.setMedecinId(appointmentDto.getDoctorId());
        
        // Parse date - le frontend envoie dateTime au format ISO
        LocalDateTime dateTime = LocalDateTime.parse(appointmentDto.getDateTime(), ISO_FORMATTER);
        consultationDto.setDateRendezVous(dateTime);
        
        // Mapping du motif et notes
        consultationDto.setMotifConsultation(appointmentDto.getReason() != null ? 
            appointmentDto.getReason() : "Consultation de routine");
        
        if (appointmentDto.getNotes() != null) {
            consultationDto.setObservationsCliniques(appointmentDto.getNotes());
        }
        
        // Statut initial
        consultationDto.setStatut(StatutConsultation.EN_ATTENTE);
        
        return consultationDto;
    }
    
    /**
     * Convertit une Consultation en AppointmentDto pour le frontend
     */
    public AppointmentDto consultationToAppointment(tn.pedialink.dossiermedical.model.consultation.Consultation consultation) {
        AppointmentDto dto = new AppointmentDto();
        
        dto.setId(consultation.getId());
        dto.setChildId(consultation.getPatientId());
        dto.setChildName(consultation.getPatientName()); // Ajouter le nom
        dto.setDoctorId(consultation.getMedecinId());
        dto.setParentId(consultation.getParentId());
        dto.setDateTime(consultation.getDateRendezVous().format(ISO_FORMATTER));
        dto.setReason(consultation.getMotifConsultation());
        dto.setNotes(consultation.getObservationsCliniques());
        
        // Mapper le statut
        dto.setStatus(mapStatutToStatus(consultation.getStatut()));
        
        return dto;
    }
    
    private String mapStatutToStatus(StatutConsultation statut) {
        switch (statut) {
            case EN_ATTENTE: return "PENDING";
            case ACCEPTEE: return "ACCEPTED";
            case REFUSEE: return "REFUSED";
            case TERMINEE: return "COMPLETED";
            case ANNULEE: return "CANCELLED";
            default: return "PENDING";
        }
    }
}
