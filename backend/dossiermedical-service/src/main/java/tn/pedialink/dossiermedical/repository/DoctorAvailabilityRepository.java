package tn.pedialink.dossiermedical.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.pedialink.dossiermedical.model.appointment.DoctorAvailability;

import java.util.Optional;

@Repository
public interface DoctorAvailabilityRepository extends MongoRepository<DoctorAvailability, String> {
    Optional<DoctorAvailability> findByDoctorId(String doctorId);
    Optional<DoctorAvailability> findByDoctorIdAndIsActiveTrue(String doctorId);
}
