package tn.pedialink.auth.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import tn.pedialink.auth.entity.Role;
import tn.pedialink.auth.entity.User;
import tn.pedialink.auth.entity.UserStatus;
import tn.pedialink.auth.repository.UserRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String adminEmail = "admin@pedialink.tn";

        if (userRepository.existsByEmail(adminEmail)) {
            log.info("Admin account already exists.");
            return;
        }

        User admin = User.builder()
                .fullName("Admin PediaLink")
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode("Admin123!"))
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();

        userRepository.save(admin);
        log.info("✅ Default admin account created: {}", adminEmail);
    }
}
