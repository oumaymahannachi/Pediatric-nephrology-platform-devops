package tn.pedialink.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tn.pedialink.auth.entity.Otp;
import tn.pedialink.auth.entity.OtpPurpose;
import tn.pedialink.auth.repository.OtpRepository;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpRepository otpRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.otp.expiration-minutes}")
    private int expirationMinutes;

    public String generateOtp(String email, OtpPurpose purpose) {
        otpRepository.deleteAllByEmailAndPurpose(email, purpose);

        String code = String.format("%06d", secureRandom.nextInt(999999));

        Otp otp = Otp.builder()
                .email(email)
                .code(code)
                .purpose(purpose)
                .expiresAt(Instant.now().plus(expirationMinutes, ChronoUnit.MINUTES))
                .used(false)
                .build();

        otpRepository.save(otp);

        log.info("========================================");
        log.info("OTP for {}: {}", email, code);
        log.info("========================================");

        return code;
    }

    public boolean verifyOtp(String email, String code, OtpPurpose purpose) {
        Optional<Otp> otpOpt = otpRepository
                .findTopByEmailAndPurposeAndUsedFalseOrderByExpiresAtDesc(email, purpose);

        if (otpOpt.isEmpty()) return false;

        Otp otp = otpOpt.get();
        if (otp.getExpiresAt().isBefore(Instant.now())) return false;
        if (!otp.getCode().equals(code)) return false;

        otpRepository.deleteById(otp.getId());
        return true;
    }
}
