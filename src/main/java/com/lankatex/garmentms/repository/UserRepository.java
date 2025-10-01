package com.lankatex.garmentms.repository;

import com.lankatex.garmentms.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // JpaRepository already has findAll(Pageable), but declaring is fine:
    Optional<User> findByUsername(String username);   // <-- needed for DataInitializer

    boolean existsByUsername(String username);        // handy if you prefer an existence check

    Page<User> findAll(Pageable pageable);
}
