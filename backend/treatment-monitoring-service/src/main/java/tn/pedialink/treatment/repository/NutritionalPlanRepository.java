package tn.pedialink.treatment.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.pedialink.treatment.entity.NutritionalPlan;

import java.util.List;

public interface NutritionalPlanRepository extends MongoRepository<NutritionalPlan, String> {

    List<NutritionalPlan> findByChildId(String childId);

    List<NutritionalPlan> findByDoctorId(String doctorId);

    long countByDoctorId(String doctorId);
}
