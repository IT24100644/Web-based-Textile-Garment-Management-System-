package com.lankatex.garmentms.boot;

import com.lankatex.garmentms.model.Role;
import com.lankatex.garmentms.model.User;
import com.lankatex.garmentms.model.enums.RoleName;
import com.lankatex.garmentms.repository.RoleRepository;
import com.lankatex.garmentms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    @Override
    public void run(ApplicationArguments args) {
        // ensure roles exist
        for (var rn : RoleName.values()) {
            roleRepo.findByName(rn).orElseGet(() -> {
                var r = new Role();
                r.setName(rn);
                return roleRepo.save(r);
            });
        }

        // seed admin if missing
        userRepo.findByUsername("admin").orElseGet(() -> {
            var admin = new User();
            admin.setUsername("admin");
            admin.setPasswordHash(encoder.encode("Admin@12345"));
            admin.setActive(true);
            var adminRole = roleRepo.findByName(RoleName.ROLE_ADMIN).orElseThrow();
            admin.setRoles(Set.of(adminRole));
            return userRepo.save(admin);
        });
    }
}
