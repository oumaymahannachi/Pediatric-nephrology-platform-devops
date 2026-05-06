package tn.pedialink.dossiermedical.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.pedialink.dossiermedical.model.examen.LabResult;
import java.util.List;

public interface LabResultRepository extends MongoRepository<LabResult, String> {
    List<LabResult> findByPatientId(String patientId);
    List<LabResult> findByMedecinId(String medecinId);
    List<LabResult> findByPatientIdOrderByTestDateDesc(String patientId);
}
