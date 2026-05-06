package tn.pedialink.treatment.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.pedialink.treatment.entity.DietaryRestriction;

import java.util.List;

public interface DietaryRestrictionRepository extends MongoRepository<DietaryRestriction, String> {

    List<DietaryRestriction> findByChildId(String childId);
}
