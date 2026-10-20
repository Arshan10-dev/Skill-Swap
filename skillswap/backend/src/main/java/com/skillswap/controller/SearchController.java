package com.skillswap.controller;

import com.skillswap.dto.Dtos.PublicUser;
import com.skillswap.model.Enums;
import com.skillswap.model.User;
import com.skillswap.model.UserSkill;
import com.skillswap.repo.BlockRepository;
import com.skillswap.repo.UserSkillRepository;
import com.skillswap.security.CurrentUser;
import com.skillswap.service.UserLookup;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/** Free search across members by skill, category, level, location and availability. */
@RestController
@RequestMapping("/api/search")
public class SearchController {
    private final UserSkillRepository userSkills;
    private final BlockRepository blocks;
    private final UserLookup lookup;

    public SearchController(UserSkillRepository userSkills, BlockRepository blocks, UserLookup lookup) {
        this.userSkills = userSkills;
        this.blocks = blocks;
        this.lookup = lookup;
    }

    public record SearchResult(PublicUser user, List<String> teaches, List<String> learns, String availableDays) {}

    @GetMapping
    @Transactional(readOnly = true)
    public List<SearchResult> search(@RequestParam(required = false) String q,
                                     @RequestParam(required = false) Long categoryId,
                                     @RequestParam(required = false) Enums.Level level,
                                     @RequestParam(required = false) String location,
                                     @RequestParam(required = false) String day,
                                     @RequestParam(defaultValue = "TEACH") Enums.SkillType type) {
        Long me = CurrentUser.id();
        Map<User, List<UserSkill>> byUser = userSkills.findByUserActiveTrue().stream()
                .collect(Collectors.groupingBy(UserSkill::getUser));
        List<SearchResult> out = new ArrayList<>();
        for (var e : byUser.entrySet()) {
            User u = e.getKey();
            if (u.getId().equals(me) || u.getRole() == Enums.Role.ADMIN || blocks.existsEitherWay(me, u.getId())) continue;
            boolean skillHit = e.getValue().stream().filter(us -> us.getType() == type).anyMatch(us ->
                    (q == null || q.isBlank() || us.getSkill().getName().toLowerCase().contains(q.toLowerCase()))
                    && (categoryId == null || us.getSkill().getCategory().getId().equals(categoryId))
                    && (level == null || us.getLevel() == level));
            if (!skillHit) continue;
            if (location != null && !location.isBlank()
                    && (u.getLocation() == null || !u.getLocation().toLowerCase().contains(location.toLowerCase()))) continue;
            if (day != null && !day.isBlank()
                    && (u.getAvailableDays() == null || !u.getAvailableDays().toUpperCase().contains(day.toUpperCase()))) continue;
            out.add(new SearchResult(lookup.pub(u),
                    names(e.getValue(), Enums.SkillType.TEACH), names(e.getValue(), Enums.SkillType.LEARN),
                    u.getAvailableDays()));
        }
        out.sort(Comparator.comparingInt((SearchResult r) -> r.user().reputation()).reversed());
        return out;
    }

    private static List<String> names(List<UserSkill> list, Enums.SkillType t) {
        return list.stream().filter(us -> us.getType() == t).map(us -> us.getSkill().getName()).sorted().toList();
    }
}
