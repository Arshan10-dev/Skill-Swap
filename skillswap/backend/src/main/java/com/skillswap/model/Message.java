package com.skillswap.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private SwapRequest swapRequest;
    @ManyToOne(optional = false)
    private User sender;
    @Column(nullable = false, length = 2000)
    private String content;
    private boolean readByRecipient = false;
    private LocalDateTime sentAt = LocalDateTime.now();

    public Long getId() { return id; }
    public SwapRequest getSwapRequest() { return swapRequest; }
    public void setSwapRequest(SwapRequest swapRequest) { this.swapRequest = swapRequest; }
    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public boolean isReadByRecipient() { return readByRecipient; }
    public void setReadByRecipient(boolean r) { this.readByRecipient = r; }
    public LocalDateTime getSentAt() { return sentAt; }
}
