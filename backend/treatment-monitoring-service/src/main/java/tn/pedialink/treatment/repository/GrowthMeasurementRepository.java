package tn.pedialink.treatment.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.pedialink.treatment.entity.GrowthMeasurement;

import java.util.List;

public interface GrowthMeasurementRepository extends MongoRepository<GrowthMeasurement, String> {

    List<GrowthMeasurement> findByChildIdOrderByDateDesc(String childId);

    long countByChildId(String childId);
}
