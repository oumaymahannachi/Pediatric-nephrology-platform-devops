package tn.pedialink.treatment.h2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.pedialink.treatment.h2.entity.DietaryRestrictionH2;

import java.util.List;
import java.util.Optional;

@Repository
public interface DietaryRestrictionH2Repository extends JpaRepository<DietaryRestrictionH2, String> {
    Optional<DietaryRestrictionH2> findByMongoId(String mongoId);
    List<DietaryRestrictionH2> findByChildId(String childId);
    List<DietaryRestrictionH2> findBySeverity(String severity);
}
