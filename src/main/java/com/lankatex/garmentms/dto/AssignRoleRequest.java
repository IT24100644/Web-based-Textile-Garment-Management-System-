package com.lankatex.garmentms.dto;

import com.lankatex.garmentms.model.enums.RoleName;
import jakarta.validation.constraints.NotNull;

public class AssignRoleRequest {
    @NotNull
    private Long userId;

    @NotNull
    private RoleName role;

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public RoleName getRole() {
        return role;
    }
    public void setRole(RoleName role) {
        this.role = role;
    }
}
