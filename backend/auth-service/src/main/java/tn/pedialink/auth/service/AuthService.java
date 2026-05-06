package tn.pedialink.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.pedialink.auth.dto.*;
import tn.pedialink.auth.entity.*;
import tn.pedialink.auth.exception.BadRequestException;
import tn.pedialink.auth.repository.LoginHistoryRepository;
import tn.pedialink.auth.repository.UserRepository;
import tn.pedialink.auth.security.JwtUtil;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;
    private final LoginHistoryRepository loginHistoryRepository;

    public ApiMessage signup(SignupRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email already registered");
        }
        if (req.getCin() != null && userRepository.existsByCin(req.getCin())) {
            throw new BadRequestException("CIN already registered");
        }

        Role role;
        try {
            role = Role.valueOf(req.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role: " + req.getRole());
        }
        if (role == Role.ADMIN) {
            throw new BadRequestException("Cannot self-register as ADMIN");
        }

        User user = User.builder()
                .fullName(req.getFullName())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(role)
                .phone(req.getPhone())
                .cin(req.getCin())
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .build();

        if (role == Role.DOCTOR) {
            user.setSpecialization(req.getSpecialization());
            user.setLicenseNumber(req.getLicenseNumber());
            user.setClinicName(req.getClinicName());
        }

        if (role == Role.INFIRMIER) {
            user.setServiceUnit(req.getServiceUnit());
        }

        userRepository.save(user);

        otpService.generateOtp(user.getEmail(), OtpPurpose.VERIFY_EMAIL);

        log.info("[TRACE] New user registered: {} (role={})", user.getEmail(), role);

        return new ApiMessage("Account created. Check your email for verification OTP.");
    }

    public AuthResponse login(LoginRequest req, String ipAddress, String userAgent) {
        User user = userRepository.findByEmail(req.getEmail()).orElse(null);

        if (user == null) {
            recordLoginAttempt(null, req.getEmail(), ipAddress, userAgent, false, "Invalid email", null);
            throw new BadRequestException("Invalid email or password");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            recordLoginAttempt(user.getId(), user.getEmail(), ipAddress, userAgent, false, "Invalid password", user.getRole());
            throw new BadRequestException("Invalid email or password");
        }

        if (user.getStatus() == UserStatus.SUSPENDED) {
            recordLoginAttempt(user.getId(), user.getEmail(), ipAddress, userAgent, false, "Account suspended", user.getRole());
            throw new BadRequestException("Account is suspended. Contact support.");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        recordLoginAttempt(user.getId(), user.getEmail(), ipAddress, userAgent, true, null, user.getRole());

        log.info("[TRACE] Successful login: {} from IP={}", user.getEmail(), ipAddress);

        return AuthResponse.builder()
                .token(token)
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    public ApiMessage requestOtp(OtpRequest req) {
        OtpPurpose purpose;
        try {
            purpose = OtpPurpose.valueOf(req.getPurpose().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid purpose. Must be VERIFY_EMAIL or RESET_PASSWORD");
        }

        userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new BadRequestException("No account found with this email"));

        otpService.generateOtp(req.getEmail(), purpose);

        return new ApiMessage("OTP sent. Check your console (dev mode).");
    }

    public AuthResponse verifyOtp(VerifyOtpRequest req) {
        OtpPurpose purpose;
        try {
            purpose = OtpPurpose.valueOf(req.getPurpose().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid purpose");
        }

        boolean valid = otpService.verifyOtp(req.getEmail(), req.getCode(), purpose);
        if (!valid) {
            throw new BadRequestException("Invalid or expired OTP");
        }

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (purpose == OtpPurpose.VERIFY_EMAIL) {
            user.setEmailVerified(true);
            userRepository.save(user);
            log.info("[TRACE] Email verified for: {}", user.getEmail());
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    public ApiMessage resetPassword(ResetPasswordRequest req) {
        boolean valid = otpService.verifyOtp(req.getEmail(), req.getCode(), OtpPurpose.RESET_PASSWORD);
        if (!valid) {
            throw new BadRequestException("Invalid or expired OTP");
        }

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        log.info("[TRACE] Password reset for: {}", user.getEmail());

        return new ApiMessage("Password reset successful. You can now log in.");
    }

    private void recordLoginAttempt(String userId, String email, String ipAddress,
                                    String userAgent, boolean success,
                                    String failureReason, Role role) {
        LoginHistory entry = LoginHistory.builder()
                .userId(userId)
                .email(email)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .success(success)
                .failureReason(failureReason)
                .role(role)
                .timestamp(Instant.now())
                .build();

        loginHistoryRepository.save(entry);

        if (!success) {
            log.warn("[SECURITY] Failed login attempt for {} from IP={} — reason: {}",
                    email, ipAddress, failureReason);
        }
    }
}
