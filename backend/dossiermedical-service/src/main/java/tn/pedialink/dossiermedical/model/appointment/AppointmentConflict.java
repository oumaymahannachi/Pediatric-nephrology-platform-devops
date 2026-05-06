package tn.pedialink.dossiermedical.model.appointment;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentConflict {
    private String conflictId;
    private String doctorId;
    private LocalDateTime conflictTime;
    private String existingAppointmentId;
    private String newAppointmentId;
    private ConflictType conflictType;
    private String resolution;
    
    public enum ConflictType {
        TIME_OVERLAP,
        DOUBLE_BOOKING,
        VACATION_DAY,
        OUTSIDE_WORKING_HOURS,
        BREAK_TIME,
        SPECIAL_DAY_UNAVAILABLE
    }
}
