package com.skillswap.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "skill_sessions")
public class SkillSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private SwapRequest swapRequest;
    @ManyToOne(optional = false)
    private User createdBy;
    @Column(nullable = false, length = 120)
    private String title;
    @Column(nullable = false)
    private LocalDateTime scheduledAt;
    private int durationMinutes = 60;
    @Enumerated(EnumType.STRING) @Column(length = 12)
    private Enums.SessionMode mode = Enums.SessionMode.ONLINE;
    @Column(length = 300)
    private String locationOrLink;
    @Column(length = 1000)
    private String notes;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 12)
    private Enums.SessionStatus status = Enums.SessionStatus.SCHEDULED;
    private boolean reminderSent = false;
    private LocalDateTime completedAt;

    public Long getId() { return id; }
    public SwapRequest getSwapRequest() { return swapRequest; }
    public void setSwapRequest(SwapRequest swapRequest) { this.swapRequest = swapRequest; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public Enums.SessionMode getMode() { return mode; }
    public void setMode(Enums.SessionMode mode) { this.mode = mode; }
    public String getLocationOrLink() { return locationOrLink; }
    public void setLocationOrLink(String l) { this.locationOrLink = l; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Enums.SessionStatus getStatus() { return status; }
    public void setStatus(Enums.SessionStatus status) { this.status = status; }
    public boolean isReminderSent() { return reminderSent; }
    public void setReminderSent(boolean reminderSent) { this.reminderSent = reminderSent; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
