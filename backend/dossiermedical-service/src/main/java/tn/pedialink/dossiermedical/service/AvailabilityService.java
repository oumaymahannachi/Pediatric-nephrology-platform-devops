package tn.pedialink.dossiermedical.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.pedialink.dossiermedical.model.appointment.*;
import tn.pedialink.dossiermedical.repository.DoctorAvailabilityRepository;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvailabilityService {
    private final DoctorAvailabilityRepository availabilityRepository;

    public DoctorAvailability createOrUpdateAvailability(DoctorAvailability availability) {
        return availabilityRepository.save(availability);
    }

    public DoctorAvailability getAvailability(String doctorId) {
        return availabilityRepository.findByDoctorIdAndIsActiveTrue(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor availability not found"));
    }

    /**
     * Génère tous les créneaux disponibles pour un médecin sur une période donnée
     */
    public List<TimeSlot> generateAvailableSlots(String doctorId, LocalDate startDate, LocalDate endDate, ConsultationType consultationType) {
        DoctorAvailability availability = getAvailability(doctorId);
        List<TimeSlot> slots = new ArrayList<>();
        
        int durationMinutes = getDurationForConsultationType(availability, consultationType);
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            // Vérifier si c'est un jour de congé
            if (availability.getVacationDays() != null && availability.getVacationDays().contains(date)) {
                continue;
            }
            
            // Vérifier les jours spéciaux
            SpecialDay specialDay = getSpecialDayForDate(availability, date);
            if (specialDay != null && !specialDay.getIsAvailable()) {
                continue;
            }
            
            // Obtenir l'horaire pour ce jour
            WeeklySchedule schedule = getScheduleForDay(availability, date.getDayOfWeek());
            if (schedule == null || !schedule.getIsAvailable()) {
                continue;
            }
            
            // Générer les créneaux pour ce jour
            slots.addAll(generateSlotsForDay(date, schedule, durationMinutes, availability.getEmergencySlotsPerDay()));
        }
        
        return slots;
    }

    /**
     * Génère les créneaux pour une journée spécifique
     */
    private List<TimeSlot> generateSlotsForDay(LocalDate date, WeeklySchedule schedule, int durationMinutes, int emergencySlots) {
        List<TimeSlot> slots = new ArrayList<>();
        LocalTime currentTime = schedule.getStartTime();
        LocalTime endTime = schedule.getEndTime();
        
        int emergencySlotsCreated = 0;
        
        while (currentTime.plusMinutes(durationMinutes).isBefore(endTime) || 
               currentTime.plusMinutes(durationMinutes).equals(endTime)) {
            
            // Vérifier si on est dans la pause déjeuner
            if (schedule.getBreakStartTime() != null && schedule.getBreakEndTime() != null) {
                if (!currentTime.isBefore(schedule.getBreakStartTime()) && 
                    currentTime.isBefore(schedule.getBreakEndTime())) {
                    currentTime = schedule.getBreakEndTime();
                    continue;
                }
            }
            
            TimeSlot slot = new TimeSlot();
            slot.setStartTime(LocalDateTime.of(date, currentTime));
            slot.setEndTime(LocalDateTime.of(date, currentTime.plusMinutes(durationMinutes)));
            slot.setIsAvailable(true);
            slot.setConsultationType(ConsultationType.STANDARD);
            
            // Réserver certains créneaux pour les urgences
            if (emergencySlotsCreated < emergencySlots && shouldBeEmergencySlot(currentTime)) {
                slot.setIsEmergencySlot(true);
                emergencySlotsCreated++;
            } else {
                slot.setIsEmergencySlot(false);
            }
            
            slots.add(slot);
            currentTime = currentTime.plusMinutes(durationMinutes);
        }
        
        return slots;
    }

    /**
     * Vérifie les conflits d'horaires
     */
    public List<AppointmentConflict> detectConflicts(String doctorId, LocalDateTime proposedStartTime, LocalDateTime proposedEndTime) {
        List<AppointmentConflict> conflicts = new ArrayList<>();
        DoctorAvailability availability = getAvailability(doctorId);
        
        LocalDate date = proposedStartTime.toLocalDate();
        LocalTime startTime = proposedStartTime.toLocalTime();
        LocalTime endTime = proposedEndTime.toLocalTime();
        
        // Vérifier jour de congé
        if (availability.getVacationDays() != null && availability.getVacationDays().contains(date)) {
            AppointmentConflict conflict = new AppointmentConflict();
            conflict.setDoctorId(doctorId);
            conflict.setConflictTime(proposedStartTime);
            conflict.setConflictType(AppointmentConflict.ConflictType.VACATION_DAY);
            conflict.setResolution("Doctor is on vacation this day");
            conflicts.add(conflict);
        }
        
        // Vérifier jour spécial
        SpecialDay specialDay = getSpecialDayForDate(availability, date);
        if (specialDay != null && !specialDay.getIsAvailable()) {
            AppointmentConflict conflict = new AppointmentConflict();
            conflict.setDoctorId(doctorId);
            conflict.setConflictTime(proposedStartTime);
            conflict.setConflictType(AppointmentConflict.ConflictType.SPECIAL_DAY_UNAVAILABLE);
            conflict.setResolution("Doctor unavailable: " + specialDay.getReason());
            conflicts.add(conflict);
        }
        
        // Vérifier horaires de travail
        WeeklySchedule schedule = getScheduleForDay(availability, date.getDayOfWeek());
        if (schedule == null || !schedule.getIsAvailable()) {
            AppointmentConflict conflict = new AppointmentConflict();
            conflict.setDoctorId(doctorId);
            conflict.setConflictTime(proposedStartTime);
            conflict.setConflictType(AppointmentConflict.ConflictType.OUTSIDE_WORKING_HOURS);
            conflict.setResolution("Doctor does not work on this day");
            conflicts.add(conflict);
        } else {
            // Vérifier si dans les heures de travail
            if (startTime.isBefore(schedule.getStartTime()) || endTime.isAfter(schedule.getEndTime())) {
                AppointmentConflict conflict = new AppointmentConflict();
                conflict.setDoctorId(doctorId);
                conflict.setConflictTime(proposedStartTime);
                conflict.setConflictType(AppointmentConflict.ConflictType.OUTSIDE_WORKING_HOURS);
                conflict.setResolution(String.format("Outside working hours (%s - %s)", 
                    schedule.getStartTime(), schedule.getEndTime()));
                conflicts.add(conflict);
            }
            
            // Vérifier pause déjeuner
            if (schedule.getBreakStartTime() != null && schedule.getBreakEndTime() != null) {
                if (!(endTime.isBefore(schedule.getBreakStartTime()) || 
                      startTime.isAfter(schedule.getBreakEndTime()))) {
                    AppointmentConflict conflict = new AppointmentConflict();
                    conflict.setDoctorId(doctorId);
                    conflict.setConflictTime(proposedStartTime);
                    conflict.setConflictType(AppointmentConflict.ConflictType.BREAK_TIME);
                    conflict.setResolution(String.format("During break time (%s - %s)", 
                        schedule.getBreakStartTime(), schedule.getBreakEndTime()));
                    conflicts.add(conflict);
                }
            }
        }
        
        return conflicts;
    }

    /**
     * Optimise le planning en suggérant le meilleur créneau
     */
    public TimeSlot suggestOptimalSlot(String doctorId, ConsultationType consultationType, PriorityLevel priority) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(30);
        
        List<TimeSlot> availableSlots = generateAvailableSlots(doctorId, startDate, endDate, consultationType);
        
        // Filtrer selon la priorité
        if (priority == PriorityLevel.CRITICAL || priority == PriorityLevel.URGENT) {
            // Chercher les créneaux d'urgence en premier
            List<TimeSlot> emergencySlots = availableSlots.stream()
                    .filter(TimeSlot::getIsEmergencySlot)
                    .filter(TimeSlot::getIsAvailable)
                    .collect(Collectors.toList());
            
            if (!emergencySlots.isEmpty()) {
                return emergencySlots.get(0);
            }
        }
        
        // Retourner le premier créneau disponible
        return availableSlots.stream()
                .filter(TimeSlot::getIsAvailable)
                .findFirst()
                .orElse(null);
    }

    // Méthodes utilitaires
    
    private int getDurationForConsultationType(DoctorAvailability availability, ConsultationType type) {
        return switch (type) {
            case STANDARD -> availability.getStandardConsultationMinutes();
            case URGENT, EMERGENCY -> availability.getUrgentConsultationMinutes();
            case FOLLOW_UP -> availability.getFollowUpConsultationMinutes();
            default -> 30;
        };
    }

    private WeeklySchedule getScheduleForDay(DoctorAvailability availability, DayOfWeek dayOfWeek) {
        if (availability.getWeeklySchedules() == null) return null;
        
        return availability.getWeeklySchedules().stream()
                .filter(s -> s.getDayOfWeek() == dayOfWeek)
                .findFirst()
                .orElse(null);
    }

    private SpecialDay getSpecialDayForDate(DoctorAvailability availability, LocalDate date) {
        if (availability.getSpecialDays() == null) return null;
        
        return availability.getSpecialDays().stream()
                .filter(s -> s.getDate().equals(date))
                .findFirst()
                .orElse(null);
    }

    private boolean shouldBeEmergencySlot(LocalTime time) {
        // Réserver les créneaux du matin et de fin d'après-midi pour les urgences
        return (time.getHour() == 9 && time.getMinute() == 0) || 
               (time.getHour() == 16 && time.getMinute() == 0);
    }
}
