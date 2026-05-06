package tn.pedialink.treatment.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.pedialink.treatment.entity.Child;

import java.util.List;

public interface ChildRepository extends MongoRepository<Child, String> {

    List<Child> findByParentId(String parentId);

    List<Child> findByDoctorIdsContaining(String doctorId);

    long countByParentId(String parentId);
}
