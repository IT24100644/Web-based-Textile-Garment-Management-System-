package com.lankatex.garmentms.service.impl;

import com.lankatex.garmentms.model.Role;
import com.lankatex.garmentms.model.User;
import com.lankatex.garmentms.model.enums.RoleName;
import com.lankatex.garmentms.repository.RoleRepository;
import com.lankatex.garmentms.repository.UserRepository;
import com.lankatex.garmentms.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;

    public UserServiceImpl(UserRepository userRepo,
                           RoleRepository roleRepo,
                           PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.encoder = encoder;
    }

    @Override
    public User createUser(String username, String rawPassword, RoleName roleName) {
        if (userRepo.existsByUsername(username)) {
            throw new IllegalArgumentException("Username is already taken");
        }
        Role role = roleRepo.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));

        User u = new User();
        u.setUsername(username);
        u.setPasswordHash(encoder.encode(rawPassword));
        u.getRoles().add(role);
        return userRepo.save(u);
    }

    @Override
    public void assignRole(Long userId, RoleName roleName) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Prevent removing your own admin ability by assigning a non-admin only (basic guard)
        String current = currentUsername();
        if (current != null && current.equals(user.getUsername()) && roleName != RoleName.ROLE_ADMIN) {
            // If you want to allow adding roles but not removing admin, you can adjust this logic later.
            // For now, we only add roles; we don't remove any.
        }

        Role role = roleRepo.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));

        user.getRoles().add(role);
        // user is managed by JPA; no explicit save required within @Transactional
    }

    @Override
    public void deactivateUser(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String current = currentUsername();
        if (current != null && current.equals(user.getUsername())) {
            throw new AccessDeniedException("You cannot deactivate your own account");
        }
        user.setActive(false);
    }

    @Override
    public void resetPassword(Long userId, String newPassword) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setPasswordHash(encoder.encode(newPassword));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> list(int page, int size) {
        return userRepo.findAll(PageRequest.of(page, size));
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? null : auth.getName();
    }
}
