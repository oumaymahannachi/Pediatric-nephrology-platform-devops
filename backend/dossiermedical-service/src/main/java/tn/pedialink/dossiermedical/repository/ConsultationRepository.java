package tn.pedialink.dossiermedical.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.pedialink.dossiermedical.model.consultation.Consultation;
import tn.pedialink.dossiermedical.model.consultation.StatutConsultation;
import java.util.List;

public interface ConsultationRepository extends MongoRepository<Consultation, String> {
    List<Consultation> findByPatientId(String patientId);
    List<Consultation> findByParentId(String parentId);
    List<Consultation> findByMedecinId(String medecinId);
    List<Consultation> findByMedecinIdAndStatut(String medecinId, StatutConsultation statut);
}
