package com.skillswap.model;

import jakarta.persistence.*;

@Entity
@Table(name = "skills")
public class Skill {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 80)
    private String name;
    @ManyToOne(optional = false)
    private Category category;

    public Skill() {}
    public Skill(String name, Category category) { this.name = name; this.category = category; }
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
}
