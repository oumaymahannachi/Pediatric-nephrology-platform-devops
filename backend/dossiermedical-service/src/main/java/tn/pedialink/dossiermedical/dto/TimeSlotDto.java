package tn.pedialink.dossiermedical.dto;

import lombok.Data;
import tn.pedialink.dossiermedical.model.appointment.ConsultationType;
import java.time.LocalDateTime;

@Data
public class TimeSlotDto {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean isAvailable;
    private Boolean isEmergencySlot;
    private ConsultationType consultationType;
    private Integer durationMinutes;
}
