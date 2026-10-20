package com.skillswap.repo;

import com.skillswap.model.Enums;
import com.skillswap.model.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {
    List<UserSkill> findByUserId(Long userId);
    List<UserSkill> findByUserIdAndType(Long userId, Enums.SkillType type);
    boolean existsByUserIdAndSkillIdAndType(Long userId, Long skillId, Enums.SkillType type);
    boolean existsBySkillId(Long skillId);
    List<UserSkill> findByUserActiveTrue();

    @Query("select us.skill.name, count(us) from UserSkill us where us.type = :type group by us.skill.name order by count(us) desc")
    List<Object[]> topSkills(Enums.SkillType type);
}
