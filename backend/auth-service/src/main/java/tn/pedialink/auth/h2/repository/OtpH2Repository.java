package tn.pedialink.auth.h2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.pedialink.auth.h2.entity.OtpH2;

import java.util.List;
import java.util.Optional;

@Repository
public interface OtpH2Repository extends JpaRepository<OtpH2, String> {

    Optional<OtpH2> findByMongoId(String mongoId);

    List<OtpH2> findByEmailOrderByExpiresAtDesc(String email);
}
