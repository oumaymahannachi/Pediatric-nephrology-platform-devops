package tn.pedialink.treatment.h2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.pedialink.treatment.h2.entity.ChildH2;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChildH2Repository extends JpaRepository<ChildH2, String> {
    Optional<ChildH2> findByMongoId(String mongoId);
    List<ChildH2> findByParentId(String parentId);
}
