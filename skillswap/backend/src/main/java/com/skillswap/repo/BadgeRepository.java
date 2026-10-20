package com.skillswap.repo;

import com.skillswap.model.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BadgeRepository extends JpaRepository<Badge, Long> {
    List<Badge> findByUserIdOrderByAwardedAtDesc(Long userId);
    boolean existsByUserIdAndCode(Long userId, String code);
}
