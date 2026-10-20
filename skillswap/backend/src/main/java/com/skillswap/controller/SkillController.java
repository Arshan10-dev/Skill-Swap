package com.skillswap.controller;

import com.skillswap.dto.Dtos.*;
import com.skillswap.model.*;
import com.skillswap.repo.*;
import com.skillswap.security.CurrentUser;
import com.skillswap.service.ApiException;
import com.skillswap.service.UserLookup;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SkillController {
    private final CategoryRepository categories;
    private final SkillRepository skills;
    private final UserSkillRepository userSkills;
    private final UserLookup lookup;
    private final com.skillswap.service.ReputationService reputation;

    public SkillController(CategoryRepository categories, SkillRepository skills, UserSkillRepository userSkills,
                           UserLookup lookup, com.skillswap.service.ReputationService reputation) {
        this.categories = categories;
        this.skills = skills;
        this.userSkills = userSkills;
        this.lookup = lookup;
        this.reputation = reputation;
    }

    @GetMapping("/categories")
    public List<CategoryDto> categories() {
        return categories.findAll().stream()
                .map(c -> new CategoryDto(c.getId(), c.getName(), c.getIcon(), skills.countByCategoryId(c.getId())))
                .toList();
    }

    @GetMapping("/skills")
    public List<SkillDto> skills(@RequestParam(required = false) Long categoryId,
                                 @RequestParam(required = false) String q) {
        List<Skill> list = categoryId != null ? skills.findByCategoryIdOrderByName(categoryId) : skills.findAllByOrderByName();
        return list.stream()
                .filter(s -> q == null || q.isBlank() || s.getName().toLowerCase().contains(q.toLowerCase()))
                .map(SkillDto::of).toList();
    }

    @GetMapping("/me/skills")
    public List<UserSkillDto> mySkills() {
        return userSkills.findByUserId(CurrentUser.id()).stream().map(UserSkillDto::of).toList();
    }

    /** Add a skill to the user's teach/learn list. Accepts an existing skillId or a new skill name + category. */
    @PostMapping("/me/skills")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public UserSkillDto addMySkill(@Valid @RequestBody AddUserSkill in) {
        Long me = CurrentUser.id();
        Skill skill;
        if (in.skillId() != null) {
            skill = skills.findById(in.skillId()).orElseThrow(() -> ApiException.notFound("Skill"));
        } else if (in.newSkillName() != null && !in.newSkillName().isBlank() && in.categoryId() != null) {
            String name = in.newSkillName().trim();
            skill = skills.findByNameIgnoreCase(name).orElseGet(() -> skills.save(new Skill(name,
                    categories.findById(in.categoryId()).orElseThrow(() -> ApiException.notFound("Category")))));
        } else {
            throw ApiException.bad("Pick a skill or enter a new skill name with a category");
        }
        if (userSkills.existsByUserIdAndSkillIdAndType(me, skill.getId(), in.type()))
            throw ApiException.conflict("This skill is already in your list");
        User u = lookup.get(me);
        UserSkill us = userSkills.save(new UserSkill(u, skill, in.type(), in.level(), in.yearsExperience()));
        reputation.evaluateBadges(u);
        return UserSkillDto.of(us);
    }

    @PutMapping("/me/skills/{id}")
    @Transactional
    public UserSkillDto updateMySkill(@PathVariable Long id, @Valid @RequestBody AddUserSkill in) {
        UserSkill us = owned(id);
        us.setLevel(in.level());
        us.setYearsExperience(in.yearsExperience());
        return UserSkillDto.of(us);
    }

    @DeleteMapping("/me/skills/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMySkill(@PathVariable Long id) { userSkills.delete(owned(id)); }

    private UserSkill owned(Long id) {
        UserSkill us = userSkills.findById(id).orElseThrow(() -> ApiException.notFound("Skill entry"));
        if (!us.getUser().getId().equals(CurrentUser.id())) throw ApiException.forbidden("Not your skill");
        return us;
    }
}
