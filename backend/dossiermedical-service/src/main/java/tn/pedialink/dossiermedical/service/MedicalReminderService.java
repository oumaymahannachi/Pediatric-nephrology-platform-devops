package tn.pedialink.dossiermedical.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.pedialink.dossiermedical.model.analytics.MedicalReminder;
import tn.pedialink.dossiermedical.model.analytics.MedicalReminder.ReminderStatus;
import tn.pedialink.dossiermedical.model.analytics.MedicalReminder.ReminderType;
import tn.pedialink.dossiermedical.model.consultation.Consultation;
import tn.pedialink.dossiermedical.model.consultation.StatutConsultation;
import tn.pedialink.dossiermedical.model.dialyse.DialysisSession;
import tn.pedialink.dossiermedical.model.dialyse.StatutSession;
import tn.pedialink.dossiermedical.model.kidney.CKDStage;
import tn.pedialink.dossiermedical.model.kidney.GFRCalculation;
import tn.pedialink.dossiermedical.repository.ConsultationRepository;
import tn.pedialink.dossiermedical.repository.DialysisSessionRepository;
import tn.pedialink.dossiermedical.repository.GFRCalculationRepository;
import tn.pedialink.dossiermedical.repository.MedicalReminderRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Service de rappels intelligents pour le suivi des patients néphropédiatriques.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MedicalReminderService {

    private final MedicalReminderRepository reminderRepository;
    private final ConsultationRepository consultationRepository;
    private final GFRCalculationRepository gfrRepository;
    private final DialysisSessionRepository dialysisSessionRepository;

    /**
     * Génère tous les rappels intelligents pour un patient.
     * Appelé après chaque consultation ou bilan.
     */
    public List<MedicalReminder> generateRemindersForPatient(String patientId, String medecinId) {
        List<MedicalReminder> generated = new ArrayList<>();

        // 1. Rappel RDV prochain
        generated.addAll(generateAppointmentReminders(patientId, medecinId));

        // 2. Suivi obligatoire patient chronique
        generated.addAll(generateMandatoryFollowupReminders(patientId, medecinId));

        // 3. Alerte inactivité
        generated.addAll(generateInactivityAlerts(patientId, medecinId));

        // 4. Rappel séance dialyse
        generated.addAll(generateDialysisReminders(patientId, medecinId));

        log.info("Generated {} reminders for patient {}", generated.size(), patientId);
        return generated;
    }

    /**
     * Rappels pour les RDV dans les 24-48h.
     */
    private List<MedicalReminder> generateAppointmentReminders(String patientId, String medecinId) {
        List<MedicalReminder> reminders = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime in48h = now.plusHours(48);

        List<Consultation> upcoming = consultationRepository.findByPatientId(patientId).stream()
            .filter(c -> c.getStatut() == StatutConsultation.ACCEPTEE)
            .filter(c -> c.getDateRendezVous() != null &&
                         c.getDateRendezVous().isAfter(now) &&
                         c.getDateRendezVous().isBefore(in48h))
            .toList();

        for (Consultation c : upcoming) {
            long hoursUntil = ChronoUnit.HOURS.between(now, c.getDateRendezVous());

            // Éviter les doublons
            List<MedicalReminder> existing = reminderRepository
                .findByPatientIdAndType(patientId, ReminderType.APPOINTMENT_REMINDER);
            boolean alreadyExists = existing.stream()
                .anyMatch(r -> r.getStatus() == ReminderStatus.PENDING &&
                               r.getDueDate() != null &&
                               Math.abs(ChronoUnit.HOURS.between(r.getDueDate(), c.getDateRendezVous())) < 2);
            if (alreadyExists) continue;

            MedicalReminder reminder = new MedicalReminder();
            reminder.setPatientId(patientId);
            reminder.setMedecinId(medecinId);
            reminder.setType(ReminderType.APPOINTMENT_REMINDER);
            reminder.setStatus(ReminderStatus.PENDING);
            reminder.setTitle("Rappel rendez-vous dans " + hoursUntil + "h");
            reminder.setMessage(String.format(
                "Rendez-vous prévu le %s. Pensez à préparer les derniers bilans biologiques.",
                c.getDateRendezVous().toLocalDate()));
            reminder.setDueDate(c.getDateRendezVous().minusHours(24));
            reminder.setCreatedAt(LocalDateTime.now());
            reminders.add(reminderRepository.save(reminder));
        }
        return reminders;
    }

    /**
     * Suivi obligatoire pour patients chroniques (CKD Stage 3+).
     * Si pas de consultation depuis > 90 jours → rappel.
     */
    private List<MedicalReminder> generateMandatoryFollowupReminders(String patientId, String medecinId) {
        List<MedicalReminder> reminders = new ArrayList<>();

        // Vérifier si patient chronique (CKD Stage 3+)
        List<GFRCalculation> gfrHistory = gfrRepository.findByPatientIdOrderByCalculationDateDesc(patientId);
        if (gfrHistory.isEmpty()) return reminders;

        GFRCalculation latestGFR = gfrHistory.get(0);
        boolean isChronic = latestGFR.getCkdStage() != null &&
                            latestGFR.getCkdStage().ordinal() >= CKDStage.STAGE_3A.ordinal();

        if (!isChronic) return reminders;

        // Dernière consultation terminée
        List<Consultation> consultations = consultationRepository.findByPatientId(patientId);
        Consultation lastCompleted = consultations.stream()
            .filter(c -> c.getStatut() == StatutConsultation.TERMINEE)
            .max(Comparator.comparing(Consultation::getDateRendezVous))
            .orElse(null);

        int maxDaysBetweenConsultations = switch (latestGFR.getCkdStage()) {
            case STAGE_5 -> 30;
            case STAGE_4 -> 45;
            case STAGE_3B -> 60;
            default -> 90;
        };

        long daysSinceLast = lastCompleted != null
            ? ChronoUnit.DAYS.between(lastCompleted.getDateRendezVous(), LocalDateTime.now())
            : 999;

        if (daysSinceLast > maxDaysBetweenConsultations) {
            // Vérifier pas de doublon
            List<MedicalReminder> existing = reminderRepository
                .findByPatientIdAndType(patientId, ReminderType.MANDATORY_FOLLOWUP);
            boolean alreadyPending = existing.stream()
                .anyMatch(r -> r.getStatus() == ReminderStatus.PENDING);
            if (alreadyPending) return reminders;

            MedicalReminder reminder = new MedicalReminder();
            reminder.setPatientId(patientId);
            reminder.setMedecinId(medecinId);
            reminder.setType(ReminderType.MANDATORY_FOLLOWUP);
            reminder.setStatus(ReminderStatus.PENDING);
            reminder.setChronic(true);
            reminder.setDaysSinceLastConsultation((int) daysSinceLast);
            reminder.setTitle("Suivi obligatoire - Patient chronique " + latestGFR.getCkdStage().getName());
            reminder.setMessage(String.format(
                "Patient %s sans consultation depuis %d jours (max recommandé: %d jours). " +
                "Planifier un rendez-vous de suivi.",
                latestGFR.getCkdStage().getName(), daysSinceLast, maxDaysBetweenConsultations));
            reminder.setDueDate(LocalDateTime.now().plusDays(7));
            reminder.setCreatedAt(LocalDateTime.now());
            reminders.add(reminderRepository.save(reminder));
        }
        return reminders;
    }

    /**
     * Alerte si patient absent depuis trop longtemps (> 6 mois).
     */
    private List<MedicalReminder> generateInactivityAlerts(String patientId, String medecinId) {
        List<MedicalReminder> reminders = new ArrayList<>();

        List<Consultation> consultations = consultationRepository.findByPatientId(patientId);
        if (consultations.isEmpty()) return reminders;

        Consultation lastAny = consultations.stream()
            .max(Comparator.comparing(Consultation::getDateRendezVous))
            .orElse(null);

        if (lastAny == null) return reminders;

        long daysSinceLast = ChronoUnit.DAYS.between(lastAny.getDateRendezVous(), LocalDateTime.now());

        if (daysSinceLast > 30) {
            List<MedicalReminder> existing = reminderRepository
                .findByPatientIdAndType(patientId, ReminderType.INACTIVITY_ALERT);
            boolean alreadyPending = existing.stream()
                .anyMatch(r -> r.getStatus() == ReminderStatus.PENDING);
            if (alreadyPending) return reminders;

            MedicalReminder reminder = new MedicalReminder();
            reminder.setPatientId(patientId);
            reminder.setMedecinId(medecinId);
            reminder.setType(ReminderType.INACTIVITY_ALERT);
            reminder.setStatus(ReminderStatus.PENDING);
            reminder.setDaysSinceLastConsultation((int) daysSinceLast);
            reminder.setTitle("Patient perdu de vue - " + daysSinceLast + " jours sans consultation");
            reminder.setMessage(String.format(
                "Aucune consultation depuis %d jours. Contacter la famille pour reprendre le suivi.",
                daysSinceLast));
            reminder.setDueDate(LocalDateTime.now().plusDays(3));
            reminder.setCreatedAt(LocalDateTime.now());
            reminders.add(reminderRepository.save(reminder));
        }
        return reminders;
    }

    /**
     * Rappels pour les prochaines séances de dialyse.
     */
    private List<MedicalReminder> generateDialysisReminders(String patientId, String medecinId) {
        List<MedicalReminder> reminders = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime in48h = now.plusHours(48);

        List<DialysisSession> upcoming = dialysisSessionRepository.findByPatientId(patientId).stream()
            .filter(s -> s.getStatus() == StatutSession.SCHEDULED)
            .filter(s -> s.getScheduledDate() != null &&
                         s.getScheduledDate().isAfter(now) &&
                         s.getScheduledDate().isBefore(in48h))
            .toList();

        for (DialysisSession session : upcoming) {
            long hoursUntil = ChronoUnit.HOURS.between(now, session.getScheduledDate());

            MedicalReminder reminder = new MedicalReminder();
            reminder.setPatientId(patientId);
            reminder.setMedecinId(medecinId);
            reminder.setType(ReminderType.DIALYSIS_SESSION);
            reminder.setStatus(ReminderStatus.PENDING);
            reminder.setTitle("Séance dialyse dans " + hoursUntil + "h");
            reminder.setMessage(String.format(
                "Séance de dialyse prévue le %s. Vérifier le poids sec et la tension artérielle.",
                session.getScheduledDate().toLocalDate()));
            reminder.setDueDate(session.getScheduledDate().minusHours(12));
            reminder.setCreatedAt(LocalDateTime.now());
            reminders.add(reminderRepository.save(reminder));
        }
        return reminders;
    }

    // ===== Queries =====

    public List<MedicalReminder> getPendingRemindersForDoctor(String medecinId) {
        return reminderRepository.findByMedecinIdAndStatusOrderByDueDateAsc(medecinId, ReminderStatus.PENDING);
    }

    public List<MedicalReminder> getPatientReminders(String patientId) {
        return reminderRepository.findByPatientIdOrderByDueDateAsc(patientId);
    }

    public MedicalReminder acknowledgeReminder(String reminderId) {
        MedicalReminder reminder = reminderRepository.findById(reminderId)
            .orElseThrow(() -> new RuntimeException("Reminder not found: " + reminderId));
        reminder.setStatus(ReminderStatus.ACKNOWLEDGED);
        reminder.setAcknowledgedAt(LocalDateTime.now());
        return reminderRepository.save(reminder);
    }

    public long countPendingForDoctor(String medecinId) {
        return reminderRepository.countByMedecinIdAndStatus(medecinId, ReminderStatus.PENDING);
    }
}
