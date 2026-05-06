package tn.pedialink.dossiermedical.model.appointment;

import lombok.Data;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
public class WeeklySchedule {
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalTime breakStartTime; // Pause déjeuner
    private LocalTime breakEndTime;
    private Boolean isAvailable = true;
}
