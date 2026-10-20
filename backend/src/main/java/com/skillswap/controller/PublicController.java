package com.skillswap.controller;

import com.skillswap.dto.Dtos.PublicStats;
import com.skillswap.model.Enums;
import com.skillswap.repo.SkillRepository;
import com.skillswap.repo.SkillSessionRepository;
import com.skillswap.repo.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PublicController {
    private final UserRepository users;
    private final SkillRepository skills;
    private final SkillSessionRepository sessions;

    public PublicController(UserRepository users, SkillRepository skills, SkillSessionRepository sessions) {
        this.users = users;
        this.skills = skills;
        this.sessions = sessions;
    }

    @GetMapping("/api/stats/public")
    public PublicStats stats() {
        return new PublicStats(users.count(), skills.count(), sessions.countByStatus(Enums.SessionStatus.COMPLETED));
    }
}
