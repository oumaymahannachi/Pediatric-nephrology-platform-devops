package tn.pedialink.treatment.h2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.pedialink.treatment.h2.entity.NutritionalPlanH2;

import java.util.List;
import java.util.Optional;

@Repository
public interface NutritionalPlanH2Repository extends JpaRepository<NutritionalPlanH2, String> {
    Optional<NutritionalPlanH2> findByMongoId(String mongoId);
    List<NutritionalPlanH2> findByChildId(String childId);
    List<NutritionalPlanH2> findByDoctorId(String doctorId);
    List<NutritionalPlanH2> findByStatus(String status);
}
