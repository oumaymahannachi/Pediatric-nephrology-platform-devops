package tn.pedialink.dossiermedical.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.pedialink.dossiermedical.model.analytics.MedicalReminder;
import tn.pedialink.dossiermedical.model.analytics.MedicalReminder.ReminderStatus;
import tn.pedialink.dossiermedical.model.analytics.MedicalReminder.ReminderType;

import java.time.LocalDateTime;
import java.util.List;

public interface MedicalReminderRepository extends MongoRepository<MedicalReminder, String> {
    List<MedicalReminder> findByPatientIdOrderByDueDateAsc(String patientId);
    List<MedicalReminder> findByMedecinIdAndStatusOrderByDueDateAsc(String medecinId, ReminderStatus status);
    List<MedicalReminder> findByStatusAndDueDateBefore(ReminderStatus status, LocalDateTime date);
    List<MedicalReminder> findByPatientIdAndType(String patientId, ReminderType type);
    long countByMedecinIdAndStatus(String medecinId, ReminderStatus status);
}
