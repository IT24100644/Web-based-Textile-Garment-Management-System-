package com.lankatex.garmentms.service;

import com.lankatex.garmentms.model.User;
import com.lankatex.garmentms.model.enums.RoleName;
import org.springframework.data.domain.Page;

public interface UserService {
    User createUser(String username, String rawPassword, RoleName roleName);
    void assignRole(Long userId, RoleName roleName);
    void deactivateUser(Long userId);
    void resetPassword(Long userId, String newPassword);
    Page<User> list(int page, int size);
}

