package tn.pedialink.dossiermedical.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.pedialink.dossiermedical.model.examen.MedicalImaging;
import java.util.List;

public interface MedicalImagingRepository extends MongoRepository<MedicalImaging, String> {
    List<MedicalImaging> findByPatientId(String patientId);
    List<MedicalImaging> findByMedecinId(String medecinId);
    List<MedicalImaging> findByPatientIdOrderByImagingDateDesc(String patientId);
}
