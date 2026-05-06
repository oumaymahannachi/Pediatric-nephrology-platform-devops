package tn.pedialink.dossiermedical.model.appointment;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class SpecialDay {
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String reason; // "Conference", "Training", "Holiday", etc.
    private Boolean isAvailable = false;
}
