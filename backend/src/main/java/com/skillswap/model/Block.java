package com.skillswap.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "blocks", uniqueConstraints = @UniqueConstraint(columnNames = {"blocker_id", "blocked_id"}))
public class Block {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private User blocker;
    @ManyToOne(optional = false)
    private User blocked;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Block() {}
    public Block(User blocker, User blocked) { this.blocker = blocker; this.blocked = blocked; }
    public Long getId() { return id; }
    public User getBlocker() { return blocker; }
    public User getBlocked() { return blocked; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
