package com.skillswap.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private User user;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private Enums.NotificationType type;
    @Column(nullable = false, length = 300)
    private String message;
    @Column(length = 120)
    private String link;
    @Column(name = "is_read")
    private boolean read = false;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Notification() {}
    public Notification(User user, Enums.NotificationType type, String message, String link) {
        this.user = user; this.type = type; this.message = message; this.link = link;
    }
    public Long getId() { return id; }
    public User getUser() { return user; }
    public Enums.NotificationType getType() { return type; }
    public String getMessage() { return message; }
    public String getLink() { return link; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
