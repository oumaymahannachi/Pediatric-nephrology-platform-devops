package tn.pedialink.dossiermedical.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.pedialink.dossiermedical.model.kidney.GFRCalculation;
import java.util.List;

@Repository
public interface GFRCalculationRepository extends MongoRepository<GFRCalculation, String> {
    List<GFRCalculation> findByPatientIdOrderByCalculationDateDesc(String patientId);
    List<GFRCalculation> findByMedecinId(String medecinId);
}
