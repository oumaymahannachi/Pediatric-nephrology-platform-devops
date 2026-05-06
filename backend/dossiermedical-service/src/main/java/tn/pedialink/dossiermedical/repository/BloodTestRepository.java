package tn.pedialink.dossiermedical.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.pedialink.dossiermedical.model.examen.BloodTest;
import java.util.List;

public interface BloodTestRepository extends MongoRepository<BloodTest, String> {
    List<BloodTest> findByPatientId(String patientId);
    List<BloodTest> findByMedecinId(String medecinId);
    List<BloodTest> findByPatientIdOrderByTestDateDesc(String patientId);
}
