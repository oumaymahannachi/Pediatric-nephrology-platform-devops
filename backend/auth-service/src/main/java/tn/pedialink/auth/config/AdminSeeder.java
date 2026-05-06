package tn.pedialink.auth.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import tn.pedialink.auth.entity.Role;
import tn.pedialink.auth.entity.User;
import tn.pedialink.auth.entity.UserStatus;
import tn.pedialink.auth.h2.service.AuthH2BackupService;
import tn.pedialink.auth.repository.UserRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthH2BackupService h2BackupService;

    @Override
    public void run(String... args) {
        seedUser("Super Admin",    "admin@pedialink.tn",  "Admin123!",  Role.ADMIN);
        seedUser("Test Doctor",    "doctor@pedialink.tn", "Doctor123!", Role.DOCTOR);
        seedUser("Test Parent",    "parent@pedialink.tn", "Parent123!", Role.PARENT);
    }

    private void seedUser(String fullName, String email, String password, Role role) {
        if (userRepository.existsByEmail(email)) {
            log.info("[SEEDER] {} already exists, skipping.", email);
            return;
        }
        User user = User.builder()
                .fullName(fullName)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .role(role)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
        userRepository.save(user);
        h2BackupService.backupUser(user);
        log.info("[SEEDER] Created user: {} ({})", email, role);
    }
}
