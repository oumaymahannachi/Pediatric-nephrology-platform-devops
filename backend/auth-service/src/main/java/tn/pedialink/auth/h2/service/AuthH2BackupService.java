package tn.pedialink.auth.h2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.pedialink.auth.entity.LoginHistory;
import tn.pedialink.auth.entity.Otp;
import tn.pedialink.auth.entity.User;
import tn.pedialink.auth.h2.entity.LoginHistoryH2;
import tn.pedialink.auth.h2.entity.OtpH2;
import tn.pedialink.auth.h2.entity.UserH2;
import tn.pedialink.auth.h2.repository.LoginHistoryH2Repository;
import tn.pedialink.auth.h2.repository.OtpH2Repository;
import tn.pedialink.auth.h2.repository.UserH2Repository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthH2BackupService {

    private final UserH2Repository userH2Repo;
    private final LoginHistoryH2Repository loginHistoryH2Repo;
    private final OtpH2Repository otpH2Repo;

    @Async
    @Transactional("h2TransactionManager")
    public void backupUser(User user) {
        try {
            UserH2 h2User = userH2Repo.findByMongoId(user.getId())
                    .orElse(UserH2.builder().mongoId(user.getId()).build());

            h2User.setFullName(user.getFullName());
            h2User.setEmail(user.getEmail());
            h2User.setPasswordHash(user.getPasswordHash());
            h2User.setRole(user.getRole() != null ? user.getRole().name() : null);
            h2User.setPhone(user.getPhone());
            h2User.setCin(user.getCin());
            h2User.setStatus(user.getStatus() != null ? user.getStatus().name() : "ACTIVE");
            h2User.setEmailVerified(user.isEmailVerified());
            h2User.setSpecialization(user.getSpecialization());
            h2User.setLicenseNumber(user.getLicenseNumber());
            h2User.setClinicName(user.getClinicName());
            h2User.setServiceUnit(user.getServiceUnit());
            h2User.setCreatedAt(user.getCreatedAt());
            h2User.setUpdatedAt(user.getUpdatedAt());

            userH2Repo.save(h2User);
            log.debug("[H2-Backup] User saved: {}", user.getId());
        } catch (Exception ex) {
            log.error("[H2-Backup] Failed to backup User {}: {}", user.getId(), ex.getMessage());
        }
    }

    @Async
    @Transactional("h2TransactionManager")
    public void deleteUser(String mongoId) {
        try {
            userH2Repo.findByMongoId(mongoId).ifPresent(u -> {
                userH2Repo.delete(u);
                log.debug("[H2-Backup] User deleted: {}", mongoId);
            });
        } catch (Exception ex) {
            log.error("[H2-Backup] Failed to delete User {}: {}", mongoId, ex.getMessage());
        }
    }

    @Async
    @Transactional("h2TransactionManager")
    public void backupLoginHistory(LoginHistory lh) {
        try {
            LoginHistoryH2 h2 = LoginHistoryH2.builder()
                    .mongoId(lh.getId())
                    .userId(lh.getUserId())
                    .email(lh.getEmail())
                    .ipAddress(lh.getIpAddress())
                    .userAgent(lh.getUserAgent())
                    .success(lh.isSuccess())
                    .failureReason(lh.getFailureReason())
                    .role(lh.getRole() != null ? lh.getRole().name() : null)
                    .timestamp(lh.getTimestamp())
                    .build();

            loginHistoryH2Repo.save(h2);
            log.debug("[H2-Backup] LoginHistory saved: {}", lh.getId());
        } catch (Exception ex) {
            log.error("[H2-Backup] Failed to backup LoginHistory {}: {}", lh.getId(), ex.getMessage());
        }
    }

    @Async
    @Transactional("h2TransactionManager")
    public void backupOtp(Otp otp) {
        try {
            OtpH2 h2 = otpH2Repo.findByMongoId(otp.getId())
                    .orElse(OtpH2.builder().mongoId(otp.getId()).build());

            h2.setEmail(otp.getEmail());
            h2.setCode(otp.getCode());
            h2.setPurpose(otp.getPurpose() != null ? otp.getPurpose().name() : null);
            h2.setExpiresAt(otp.getExpiresAt());
            h2.setUsed(otp.isUsed());

            otpH2Repo.save(h2);
            log.debug("[H2-Backup] OTP saved: {}", otp.getId());
        } catch (Exception ex) {
            log.error("[H2-Backup] Failed to backup OTP {}: {}", otp.getId(), ex.getMessage());
        }
    }
}
