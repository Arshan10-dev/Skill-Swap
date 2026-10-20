package com.skillswap.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_skills", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "skill_id", "type"}))
public class UserSkill {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private User user;
    @ManyToOne(optional = false)
    private Skill skill;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10)
    private Enums.SkillType type;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 15)
    private Enums.Level level = Enums.Level.BEGINNER;
    private int yearsExperience;

    public UserSkill() {}
    public UserSkill(User user, Skill skill, Enums.SkillType type, Enums.Level level, int years) {
        this.user = user; this.skill = skill; this.type = type; this.level = level; this.yearsExperience = years;
    }
    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Skill getSkill() { return skill; }
    public void setSkill(Skill skill) { this.skill = skill; }
    public Enums.SkillType getType() { return type; }
    public void setType(Enums.SkillType type) { this.type = type; }
    public Enums.Level getLevel() { return level; }
    public void setLevel(Enums.Level level) { this.level = level; }
    public int getYearsExperience() { return yearsExperience; }
    public void setYearsExperience(int yearsExperience) { this.yearsExperience = yearsExperience; }
}
