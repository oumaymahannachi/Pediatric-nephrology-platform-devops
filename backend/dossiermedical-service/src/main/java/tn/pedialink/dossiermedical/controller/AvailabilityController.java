package tn.pedialink.dossiermedical.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.pedialink.dossiermedical.model.appointment.*;
import tn.pedialink.dossiermedical.service.AvailabilityService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
public class AvailabilityController {
    private final AvailabilityService availabilityService;

    @PostMapping
    public ResponseEntity<DoctorAvailability> createOrUpdateAvailability(@RequestBody DoctorAvailability availability) {
        return ResponseEntity.ok(availabilityService.createOrUpdateAvailability(availability));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<DoctorAvailability> getAvailability(@PathVariable String doctorId) {
        return ResponseEntity.ok(availabilityService.getAvailability(doctorId));
    }

    @GetMapping("/doctor/{doctorId}/slots")
    public ResponseEntity<List<TimeSlot>> getAvailableSlots(
            @PathVariable String doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "STANDARD") ConsultationType consultationType) {
        
        List<TimeSlot> slots = availabilityService.generateAvailableSlots(doctorId, startDate, endDate, consultationType);
        return ResponseEntity.ok(slots);
    }

    @PostMapping("/doctor/{doctorId}/check-conflicts")
    public ResponseEntity<List<AppointmentConflict>> checkConflicts(
            @PathVariable String doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        List<AppointmentConflict> conflicts = availabilityService.detectConflicts(doctorId, startTime, endTime);
        return ResponseEntity.ok(conflicts);
    }

    @GetMapping("/doctor/{doctorId}/suggest-slot")
    public ResponseEntity<TimeSlot> suggestOptimalSlot(
            @PathVariable String doctorId,
            @RequestParam(defaultValue = "STANDARD") ConsultationType consultationType,
            @RequestParam(defaultValue = "NORMAL") PriorityLevel priority) {
        
        TimeSlot slot = availabilityService.suggestOptimalSlot(doctorId, consultationType, priority);
        return ResponseEntity.ok(slot);
    }
}
