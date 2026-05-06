package tn.pedialink.treatment.h2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.pedialink.treatment.h2.entity.GrowthMeasurementH2;

import java.util.List;
import java.util.Optional;

@Repository
public interface GrowthMeasurementH2Repository extends JpaRepository<GrowthMeasurementH2, String> {
    Optional<GrowthMeasurementH2> findByMongoId(String mongoId);
    List<GrowthMeasurementH2> findByChildIdOrderByDateDesc(String childId);
}
