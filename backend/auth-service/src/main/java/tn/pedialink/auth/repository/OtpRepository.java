package tn.pedialink.auth.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.pedialink.auth.entity.Otp;
import tn.pedialink.auth.entity.OtpPurpose;

import java.util.Optional;

public interface OtpRepository extends MongoRepository<Otp, String> {
    Optional<Otp> findTopByEmailAndPurposeAndUsedFalseOrderByExpiresAtDesc(String email, OtpPurpose purpose);
    void deleteAllByEmailAndPurpose(String email, OtpPurpose purpose);
}
