package com.lankatex.garmentms.repository;

import com.lankatex.garmentms.model.Role;
import com.lankatex.garmentms.model.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
