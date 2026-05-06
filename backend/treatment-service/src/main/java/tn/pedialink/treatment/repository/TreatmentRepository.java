package tn.pedialink.treatment.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.pedialink.treatment.entity.Treatment;

import java.util.List;

@Repository
public interface TreatmentRepository extends MongoRepository<Treatment, String> {
    List<Treatment> findByPatientId(String patientId);
    List<Treatment> findByMedecinId(String medecinId);
}
