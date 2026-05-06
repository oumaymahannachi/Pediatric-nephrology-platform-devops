package tn.pedialink.treatment.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.pedialink.treatment.entity.ProgressLog;

import java.time.LocalDate;
import java.util.List;

public interface ProgressLogRepository extends MongoRepository<ProgressLog, String> {
    List<ProgressLog> findByTreatmentIdOrderByDateDesc(String treatmentId);
    List<ProgressLog> findByPatientIdAndDateBetween(String patientId, LocalDate start, LocalDate end);
    List<ProgressLog> findByPatientIdOrderByDateDesc(String patientId);
}
