package com.skillswap.repo;

import com.skillswap.model.Block;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface BlockRepository extends JpaRepository<Block, Long> {
    List<Block> findByBlockerId(Long blockerId);
    Optional<Block> findByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    @Query("select count(b) > 0 from Block b where (b.blocker.id = :a and b.blocked.id = :b) or (b.blocker.id = :b and b.blocked.id = :a)")
    boolean existsEitherWay(Long a, Long b);
}
