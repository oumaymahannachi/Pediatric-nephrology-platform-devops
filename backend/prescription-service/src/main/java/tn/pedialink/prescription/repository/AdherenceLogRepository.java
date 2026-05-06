package tn.pedialink.prescription.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.pedialink.prescription.model.AdherenceLog;

import java.time.LocalDateTime;
import java.util.List;

public interface AdherenceLogRepository extends MongoRepository<AdherenceLog, String> {
    List<AdherenceLog> findByPrescriptionIdOrderByDatePriseDesc(String prescriptionId);
    List<AdherenceLog> findByPatientIdAndDatePriseBetween(String patientId, LocalDateTime start, LocalDateTime end);
    List<AdherenceLog> findByPatientIdOrderByDatePriseDesc(String patientId);
}
