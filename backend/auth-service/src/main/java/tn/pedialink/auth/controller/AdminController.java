package tn.pedialink.auth.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.pedialink.auth.dto.ApiMessage;
import tn.pedialink.auth.dto.LoginHistoryDto;
import tn.pedialink.auth.dto.UserDto;
import tn.pedialink.auth.entity.Role;
import tn.pedialink.auth.entity.User;
import tn.pedialink.auth.entity.UserStatus;
import tn.pedialink.auth.exception.BadRequestException;
import tn.pedialink.auth.repository.UserRepository;
import tn.pedialink.auth.service.LoginHistoryService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final LoginHistoryService loginHistoryService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard() {
        long totalUsers = userRepository.count();
        long doctorsCount = userRepository.countByRole(Role.DOCTOR);
        long parentsCount = userRepository.countByRole(Role.PARENT);
        long nursesCount = userRepository.countByRole(Role.INFIRMIER);
        long totalLogins = loginHistoryService.countTotalLogins();
        long failedLogins = loginHistoryService.countFailedAttempts();

        return ResponseEntity.ok(Map.of(
                "totalUsers", totalUsers,
                "doctorsCount", doctorsCount,
                "parentsCount", parentsCount,
                "nursesCount", nursesCount,
                "childrenCount", 0L,
                "appointmentsCount", 0L,
                "totalLogins", totalLogins,
                "failedLogins", failedLogins
        ));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("User not found"));
        return ResponseEntity.ok(toDto(user));
    }

    @GetMapping("/users/role/{role}")
    public ResponseEntity<List<UserDto>> getUsersByRole(@PathVariable String role) {
        Role r;
        try {
            r = Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role");
        }
        List<UserDto> users = userRepository.findAllByRole(r).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{id}/ban")
    public ResponseEntity<ApiMessage> banUser(@PathVariable String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("User not found"));
        if (user.getRole() == Role.ADMIN) {
            throw new BadRequestException("Cannot ban an admin");
        }
        user.setStatus(UserStatus.SUSPENDED);
        userRepository.save(user);
        return ResponseEntity.ok(new ApiMessage("User banned successfully"));
    }

    @PutMapping("/users/{id}/unban")
    public ResponseEntity<ApiMessage> unbanUser(@PathVariable String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("User not found"));
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        return ResponseEntity.ok(new ApiMessage("User unbanned successfully"));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable String id, @Valid @RequestBody AdminUpdateRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("User not found"));
        user.setFullName(req.getFullName());
        user.setPhone(req.getPhone());
        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            user.setEmail(req.getEmail());
        }
        if (req.getCin() != null && !req.getCin().isBlank()) {
            user.setCin(req.getCin());
        }
        if (user.getRole() == Role.DOCTOR) {
            if (req.getSpecialization() != null) user.setSpecialization(req.getSpecialization());
            if (req.getLicenseNumber() != null) user.setLicenseNumber(req.getLicenseNumber());
            if (req.getClinicName() != null) user.setClinicName(req.getClinicName());
        }
        if (user.getRole() == Role.INFIRMIER) {
            if (req.getServiceUnit() != null) user.setServiceUnit(req.getServiceUnit());
        }
        userRepository.save(user);
        return ResponseEntity.ok(toDto(user));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiMessage> deleteUser(@PathVariable String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("User not found"));
        if (user.getRole() == Role.ADMIN) {
            throw new BadRequestException("Cannot delete an admin");
        }
        userRepository.delete(user);
        return ResponseEntity.ok(new ApiMessage("User deleted"));
    }

    @GetMapping("/login-history")
    public ResponseEntity<List<LoginHistoryDto>> getLoginHistory() {
        return ResponseEntity.ok(loginHistoryService.getAllLoginHistory());
    }

    @GetMapping("/login-history/user/{userId}")
    public ResponseEntity<List<LoginHistoryDto>> getLoginHistoryByUser(@PathVariable String userId) {
        return ResponseEntity.ok(loginHistoryService.getLoginHistoryByUser(userId));
    }

    @GetMapping("/login-history/failed")
    public ResponseEntity<List<LoginHistoryDto>> getFailedLoginAttempts() {
        return ResponseEntity.ok(loginHistoryService.getFailedAttempts());
    }

    @GetMapping("/login-history/recent")
    public ResponseEntity<List<LoginHistoryDto>> getRecentLoginHistory(
            @RequestParam(defaultValue = "24") int hours) {
        return ResponseEntity.ok(loginHistoryService.getRecentLoginHistory(hours));
    }

    private UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .phone(user.getPhone())
                .cin(user.getCin())
                .status(user.getStatus().name())
                .emailVerified(user.isEmailVerified())
                .specialization(user.getSpecialization())
                .licenseNumber(user.getLicenseNumber())
                .clinicName(user.getClinicName())
                .serviceUnit(user.getServiceUnit())
                .build();
    }

    @Data
    public static class AdminUpdateRequest {
        @NotBlank @Size(min = 2, max = 100)
        private String fullName;
        private String email;
        @Pattern(regexp = "\\d{8}", message = "CIN must be 8 digits")
        private String cin;
        private String phone;
        private String specialization;
        private String licenseNumber;
        private String clinicName;
        private String serviceUnit;
    }
}
