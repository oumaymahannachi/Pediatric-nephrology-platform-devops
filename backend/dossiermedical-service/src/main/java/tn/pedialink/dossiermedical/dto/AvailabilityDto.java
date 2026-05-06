package tn.pedialink.dossiermedical.dto;

import lombok.Data;
import tn.pedialink.dossiermedical.model.appointment.WeeklySchedule;
import tn.pedialink.dossiermedical.model.appointment.SpecialDay;
import java.time.LocalDate;
import java.util.List;

@Data
public class AvailabilityDto {
    private String doctorId;
    private String doctorName;
    private List<WeeklySchedule> weeklySchedules;
    private List<LocalDate> vacationDays;
    private List<SpecialDay> specialDays;
    private Integer standardConsultationMinutes;
    private Integer urgentConsultationMinutes;
    private Integer followUpConsultationMinutes;
    private Integer emergencySlotsPerDay;
}
