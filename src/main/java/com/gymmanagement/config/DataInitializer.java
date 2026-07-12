package com.gymmanagement.config;

import com.gymmanagement.entity.Role;
import com.gymmanagement.entity.User;
import com.gymmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds a default Admin account on application startup so the system is
 * usable immediately without any manual registration step:
 *
 *   Login ID: admin
 *   Password: admin123
 *
 * This runs once — if a user with username "admin" already exists
 * (e.g. on every subsequent restart), nothing happens.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.existsByUsername(DEFAULT_ADMIN_USERNAME)) {
            return;
        }

        User admin = User.builder()
                .name("Administrator")
                .username(DEFAULT_ADMIN_USERNAME)
                .password(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD))
                .role(Role.ADMIN)
                .enabled(true)
                .build();

        userRepository.save(admin);
        log.info("==================================================================");
        log.info(" Default admin account created — Login ID: {}  Password: {}", DEFAULT_ADMIN_USERNAME, DEFAULT_ADMIN_PASSWORD);
        log.info(" Please change this password after first login.");
        log.info("==================================================================");
    }
}
