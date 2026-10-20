package com.skillswap.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews", uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "reviewer_id"}))
public class Review {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private SkillSession session;
    @ManyToOne(optional = false)
    private User reviewer;
    @ManyToOne(optional = false)
    private User reviewee;
    @Column(nullable = false)
    private int rating;
    @Column(length = 1000)
    private String comment;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public SkillSession getSession() { return session; }
    public void setSession(SkillSession session) { this.session = session; }
    public User getReviewer() { return reviewer; }
    public void setReviewer(User reviewer) { this.reviewer = reviewer; }
    public User getReviewee() { return reviewee; }
    public void setReviewee(User reviewee) { this.reviewee = reviewee; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
