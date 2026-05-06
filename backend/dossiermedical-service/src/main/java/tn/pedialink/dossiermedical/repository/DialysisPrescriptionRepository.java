package tn.pedialink.dossiermedical.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.pedialink.dossiermedical.model.dialyse.DialysisPrescription;
import java.util.List;

public interface DialysisPrescriptionRepository extends MongoRepository<DialysisPrescription, String> {
    List<DialysisPrescription> findByPatientId(String patientId);
    List<DialysisPrescription> findByMedecinId(String medecinId);
    List<DialysisPrescription> findByPatientIdAndActive(String patientId, Boolean active);
}
