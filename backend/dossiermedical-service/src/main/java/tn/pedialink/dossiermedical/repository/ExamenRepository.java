package tn.pedialink.dossiermedical.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.pedialink.dossiermedical.model.examen.Examen;
import tn.pedialink.dossiermedical.model.examen.TypeExamen;
import java.util.List;

public interface ExamenRepository extends MongoRepository<Examen, String> {
    List<Examen> findByPatientId(String patientId);
    List<Examen> findByPatientIdAndTypeExamen(String patientId, TypeExamen typeExamen);
    List<Examen> findByConsultationId(String consultationId);
}
