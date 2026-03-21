package com.github.martinfrank.vbuddy.security;

import com.github.martinfrank.vbuddy.model.AppUser;
import com.github.martinfrank.vbuddy.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class InitialAdminSetup implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InitialAdminSetup.class);

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${vbuddy.admin.username}")
    private String adminUsername;

    @Value("${vbuddy.admin.password}")
    private String adminPassword;

    public InitialAdminSetup(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!appUserRepository.existsByUsername(adminUsername)) {
            AppUser admin = new AppUser();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole("ADMIN");
            appUserRepository.save(admin);
            log.info("Initial admin user '{}' created", adminUsername);
        } else {
            log.info("Admin user '{}' already exists, skipping creation", adminUsername);
        }
    }
}
