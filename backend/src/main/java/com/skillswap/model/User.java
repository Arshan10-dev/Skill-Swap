package com.skillswap.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, unique = true, length = 150)
    private String email;
    @Column(nullable = false)
    private String password;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10)
    private Enums.Role role = Enums.Role.USER;
    @Column(length = 1000)
    private String bio;
    @Column(length = 100)
    private String location;
    /** Comma separated days, e.g. MON,WED,SAT */
    @Column(length = 60)
    private String availableDays;
    /** MORNING, AFTERNOON, EVENING, FLEXIBLE */
    @Column(length = 20)
    private String preferredTime;
    /** ONLINE, IN_PERSON, BOTH */
    @Column(length = 20)
    private String preferredMode = "ONLINE";
    private int reputation = 0;
    private boolean active = true;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Enums.Role getRole() { return role; }
    public void setRole(Enums.Role role) { this.role = role; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getAvailableDays() { return availableDays; }
    public void setAvailableDays(String availableDays) { this.availableDays = availableDays; }
    public String getPreferredTime() { return preferredTime; }
    public void setPreferredTime(String preferredTime) { this.preferredTime = preferredTime; }
    public String getPreferredMode() { return preferredMode; }
    public void setPreferredMode(String preferredMode) { this.preferredMode = preferredMode; }
    public int getReputation() { return reputation; }
    public void setReputation(int reputation) { this.reputation = reputation; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
