package tn.pedialink.prescription.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import tn.pedialink.prescription.model.Prescription;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends MongoRepository<Prescription, String> {

    List<Prescription> findByPatientIdOrderByDatePrescriptionDesc(String patientId);

    List<Prescription> findByMedecinIdAndStatut(String medecinId, Prescription.StatutPrescription statut);

    @Query("{ 'patientId': ?0, 'statut': 'ACTIVE', 'dateExpiration': { $gte: ?1 } }")
    List<Prescription> findActiveByPatientId(String patientId, LocalDate currentDate);

    @Query("{ 'statut': 'ACTIVE', 'dateExpiration': { $gte: ?0, $lte: ?1 } }")
    List<Prescription> findExpiringBetween(LocalDate start, LocalDate end);

    @Query("{ 'statut': 'ACTIVE', 'dateExpiration': { $lt: ?0 } }")
    List<Prescription> findExpiredButNotUpdated(LocalDate currentDate);

    Optional<Prescription> findByIdAndPatientId(String id, String patientId);
}