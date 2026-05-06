package tn.pedialink.dossiermedical.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.pedialink.dossiermedical.model.dialyse.Dialyse;
import java.util.List;

public interface DialyseRepository extends MongoRepository<Dialyse, String> {
    List<Dialyse> findByPatientId(String patientId);
    List<Dialyse> findByConsultationId(String consultationId);
}
