package com.skillswap.repo;

import com.skillswap.model.Enums;
import com.skillswap.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    List<User> findByRoleAndActiveTrue(Enums.Role role);
    long countByCreatedAtAfter(LocalDateTime t);
    long countByActiveFalse();
}
