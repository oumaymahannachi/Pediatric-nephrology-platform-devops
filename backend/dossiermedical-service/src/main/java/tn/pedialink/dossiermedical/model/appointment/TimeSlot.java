package tn.pedialink.dossiermedical.model.appointment;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TimeSlot {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean isAvailable;
    private Boolean isEmergencySlot;
    private String appointmentId; // Si déjà réservé
    private ConsultationType consultationType;
}
