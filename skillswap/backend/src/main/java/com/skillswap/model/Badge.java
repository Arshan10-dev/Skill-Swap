package com.skillswap.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "badges", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "code"}))
public class Badge {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private User user;
    @Column(nullable = false, length = 40)
    private String code;
    @Column(nullable = false, length = 80)
    private String name;
    @Column(length = 200)
    private String description;
    private LocalDateTime awardedAt = LocalDateTime.now();

    public Badge() {}
    public Badge(User user, String code, String name, String description) {
        this.user = user; this.code = code; this.name = name; this.description = description;
    }
    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public LocalDateTime getAwardedAt() { return awardedAt; }
}
