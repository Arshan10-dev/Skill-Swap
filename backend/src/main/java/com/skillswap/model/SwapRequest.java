package com.skillswap.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "swap_requests")
public class SwapRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private User sender;
    @ManyToOne(optional = false)
    private User receiver;
    /** Skill the sender will teach */
    @ManyToOne
    private Skill offeredSkill;
    /** Skill the sender wants to learn from the receiver */
    @ManyToOne
    private Skill requestedSkill;
    @Column(length = 500)
    private String message;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 12)
    private Enums.RequestStatus status = Enums.RequestStatus.PENDING;
    private int compatibilityScore;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime respondedAt;

    public Long getId() { return id; }
    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }
    public User getReceiver() { return receiver; }
    public void setReceiver(User receiver) { this.receiver = receiver; }
    public Skill getOfferedSkill() { return offeredSkill; }
    public void setOfferedSkill(Skill offeredSkill) { this.offeredSkill = offeredSkill; }
    public Skill getRequestedSkill() { return requestedSkill; }
    public void setRequestedSkill(Skill requestedSkill) { this.requestedSkill = requestedSkill; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Enums.RequestStatus getStatus() { return status; }
    public void setStatus(Enums.RequestStatus status) { this.status = status; }
    public int getCompatibilityScore() { return compatibilityScore; }
    public void setCompatibilityScore(int compatibilityScore) { this.compatibilityScore = compatibilityScore; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getRespondedAt() { return respondedAt; }
    public void setRespondedAt(LocalDateTime respondedAt) { this.respondedAt = respondedAt; }
    public boolean involves(Long userId) { return sender.getId().equals(userId) || receiver.getId().equals(userId); }
    public User other(Long userId) { return sender.getId().equals(userId) ? receiver : sender; }
}
