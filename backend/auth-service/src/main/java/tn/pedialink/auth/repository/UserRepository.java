package tn.pedialink.auth.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.pedialink.auth.entity.Role;
import tn.pedialink.auth.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByCin(String cin);
    List<User> findAllByRole(Role role);
    long countByRole(Role role);
}
