package tn.pedialink.auth.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tn.pedialink.auth.dto.UserDto;
import tn.pedialink.auth.entity.Role;
import tn.pedialink.auth.entity.User;
import tn.pedialink.auth.entity.UserStatus;
import tn.pedialink.auth.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<UserDto> me() {
        User user = getCurrentUser();
        return ResponseEntity.ok(toDto(user));
    }

    @GetMapping("/doctors")
    public ResponseEntity<List<UserDto>> getAvailableDoctors() {
        List<UserDto> doctors = userRepository.findAllByRole(Role.DOCTOR).stream()
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(doctors);
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto> updateProfile(@Valid @RequestBody UpdateProfileRequest req) {
        User user = getCurrentUser();

        user.setFullName(req.getFullName());
        user.setPhone(req.getPhone());

        if ("DOCTOR".equals(user.getRole().name())) {
            if (req.getSpecialization() != null) user.setSpecialization(req.getSpecialization());
            if (req.getLicenseNumber() != null) user.setLicenseNumber(req.getLicenseNumber());
            if (req.getClinicName() != null) user.setClinicName(req.getClinicName());
        }

        if ("INFIRMIER".equals(user.getRole().name())) {
            if (req.getServiceUnit() != null) user.setServiceUnit(req.getServiceUnit());
        }

        userRepository.save(user);
        return ResponseEntity.ok(toDto(user));
    }

    private User getCurrentUser() {
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .phone(user.getPhone())
                .cin(user.getCin())
                .status(user.getStatus() != null ? user.getStatus().name() : null)
                .emailVerified(user.isEmailVerified())
                .specialization(user.getSpecialization())
                .licenseNumber(user.getLicenseNumber())
                .clinicName(user.getClinicName())
                .serviceUnit(user.getServiceUnit())
                .build();
    }

    @Data
    public static class UpdateProfileRequest {
        @NotBlank @Size(min = 2, max = 100)
        private String fullName;
        private String phone;
        private String specialization;
        private String licenseNumber;
        private String clinicName;
        private String serviceUnit;
    }
}
