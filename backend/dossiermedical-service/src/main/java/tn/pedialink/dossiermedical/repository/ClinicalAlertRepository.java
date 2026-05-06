package tn.pedialink.dossiermedical.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.pedialink.dossiermedical.model.analytics.ClinicalAlert;
import java.util.List;

public interface ClinicalAlertRepository extends MongoRepository<ClinicalAlert, String> {
    List<ClinicalAlert> findByPatientIdOrderByCreatedAtDesc(String patientId);
    List<ClinicalAlert> findByMedecinIdAndAcknowledgedFalseOrderByCreatedAtDesc(String medecinId);
    List<ClinicalAlert> findByPatientIdAndAcknowledgedFalse(String patientId);
    List<ClinicalAlert> findBySeverityAndAcknowledgedFalse(ClinicalAlert.AlertSeverity severity);
}
