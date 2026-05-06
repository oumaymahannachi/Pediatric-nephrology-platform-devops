package tn.pedialink.auth.h2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.pedialink.auth.h2.entity.UserH2;

import java.util.Optional;

@Repository
public interface UserH2Repository extends JpaRepository<UserH2, String> {

    Optional<UserH2> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<UserH2> findByMongoId(String mongoId);
}
