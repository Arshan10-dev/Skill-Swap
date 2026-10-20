package com.skillswap.service;

import com.skillswap.dto.Dtos.*;
import com.skillswap.model.Enums;
import com.skillswap.model.User;
import com.skillswap.model.UserSkill;
import com.skillswap.repo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProfileService {
    private final UserSkillRepository userSkills;
    private final BadgeRepository badges;
    private final SkillSessionRepository sessions;
    private final ReviewRepository reviews;
    private final BlockRepository blocks;
    private final UserLookup lookup;

    public ProfileService(UserSkillRepository userSkills, BadgeRepository badges, SkillSessionRepository sessions,
                          ReviewRepository reviews, BlockRepository blocks, UserLookup lookup) {
        this.userSkills = userSkills;
        this.badges = badges;
        this.sessions = sessions;
        this.reviews = reviews;
        this.blocks = blocks;
        this.lookup = lookup;
    }

    @Transactional(readOnly = true)
    public ProfileResponse build(Long userId, Long viewerId) {
        User u = lookup.get(userId);
        boolean self = userId.equals(viewerId);
        List<UserSkill> all = userSkills.findByUserId(userId);
        return new ProfileResponse(u.getId(), u.getName(), self ? u.getEmail() : null, u.getRole().name(), u.getBio(),
                u.getLocation(), u.getAvailableDays(), u.getPreferredTime(), u.getPreferredMode(), u.getReputation(),
                lookup.avg(userId), sessions.countCompletedForUser(userId),
                reviews.findByRevieweeIdOrderByCreatedAtDesc(userId).size(), u.getCreatedAt(),
                all.stream().filter(s -> s.getType() == Enums.SkillType.TEACH).map(UserSkillDto::of).toList(),
                all.stream().filter(s -> s.getType() == Enums.SkillType.LEARN).map(UserSkillDto::of).toList(),
                badges.findByUserIdOrderByAwardedAtDesc(userId).stream().map(BadgeDto::of).toList(),
                !self && blocks.findByBlockerIdAndBlockedId(viewerId, userId).isPresent());
    }
}
