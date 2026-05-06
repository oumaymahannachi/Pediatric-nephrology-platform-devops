package tn.pedialink.dossiermedical.model.appointment;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Document(collection = "doctor_availability")
public class DoctorAvailability {
    @Id
    private String id;
    private String doctorId;
    private String doctorName;
    
    // Horaires réguliers
    private List<WeeklySchedule> weeklySchedules;
    
    // Jours de congé
    private List<LocalDate> vacationDays;
    
    // Exceptions (jours spéciaux)
    private List<SpecialDay> specialDays;
    
    // Durées par type de consultation
    private Integer standardConsultationMinutes = 30;
    private Integer urgentConsultationMinutes = 45;
    private Integer followUpConsultationMinutes = 20;
    
    // Créneaux réservés pour urgences
    private Integer emergencySlotsPerDay = 2;
    
    private Boolean isActive = true;
}
