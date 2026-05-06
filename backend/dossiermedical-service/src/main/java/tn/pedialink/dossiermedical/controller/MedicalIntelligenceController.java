package tn.pedialink.dossiermedical.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.pedialink.dossiermedical.model.analytics.BiologicalInterpretation;
import tn.pedialink.dossiermedical.model.analytics.MedicalReminder;
import tn.pedialink.dossiermedical.model.analytics.PathologyEvolution;
import tn.pedialink.dossiermedical.service.BiologicalInterpretationService;
import tn.pedialink.dossiermedical.service.MedicalReminderService;
import tn.pedialink.dossiermedical.service.PathologyEvolutionService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medical-intelligence")
@RequiredArgsConstructor
public class MedicalIntelligenceController {

    private final BiologicalInterpretationService interpretationService;
    private final PathologyEvolutionService evolutionService;
    private final MedicalReminderService reminderService;

    @GetMapping("/biological-interpretation/{patientId}")
    public ResponseEntity<BiologicalInterpretation> interpretBiologicalResults(
            @PathVariable String patientId,
            @RequestParam int ageMonths,
            @RequestParam double weightKg) {
        return ResponseEntity.ok(interpretationService.interpret(patientId, ageMonths, weightKg));
    }

    @GetMapping("/pathology-evolution/{patientId}")
    public ResponseEntity<PathologyEvolution> getPathologyEvolution(@PathVariable String patientId) {
        return ResponseEntity.ok(evolutionService.analyzeEvolution(patientId));
    }

    @PostMapping("/reminders/generate/{patientId}")
    public ResponseEntity<List<MedicalReminder>> generateReminders(
            @PathVariable String patientId,
            @RequestBody Map<String, String> body) {
        String medecinId = body.getOrDefault("medecinId", "unknown");
        return ResponseEntity.ok(reminderService.generateRemindersForPatient(patientId, medecinId));
    }

    @GetMapping("/reminders/doctor/{medecinId}")
    public ResponseEntity<List<MedicalReminder>> getDoctorReminders(@PathVariable String medecinId) {
        return ResponseEntity.ok(reminderService.getPendingRemindersForDoctor(medecinId));
    }

    @GetMapping("/reminders/patient/{patientId}")
    public ResponseEntity<List<MedicalReminder>> getPatientReminders(@PathVariable String patientId) {
        return ResponseEntity.ok(reminderService.getPatientReminders(patientId));
    }

    @PutMapping("/reminders/{reminderId}/acknowledge")
    public ResponseEntity<MedicalReminder> acknowledgeReminder(@PathVariable String reminderId) {
        return ResponseEntity.ok(reminderService.acknowledgeReminder(reminderId));
    }

    @GetMapping("/reminders/doctor/{medecinId}/count")
    public ResponseEntity<Map<String, Long>> countPendingReminders(@PathVariable String medecinId) {
        return ResponseEntity.ok(Map.of("pending", reminderService.countPendingForDoctor(medecinId)));
    }
}
