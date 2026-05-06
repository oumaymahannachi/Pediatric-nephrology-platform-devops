package tn.pedialink.dossiermedical.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.pedialink.dossiermedical.model.dialyse.DialysisSession;
import tn.pedialink.dossiermedical.model.dialyse.StatutSession;
import java.time.LocalDateTime;
import java.util.List;

public interface DialysisSessionRepository extends MongoRepository<DialysisSession, String> {
    List<DialysisSession> findByPatientId(String patientId);
    List<DialysisSession> findByPrescriptionId(String prescriptionId);
    List<DialysisSession> findByMedecinId(String medecinId);
    List<DialysisSession> findByStatus(StatutSession status);
    List<DialysisSession> findByScheduledDateBetween(LocalDateTime start, LocalDateTime end);
    List<DialysisSession> findByPatientIdAndScheduledDateBetween(String patientId, LocalDateTime start, LocalDateTime end);
}
