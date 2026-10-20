package com.skillswap.service;

import com.skillswap.model.Enums;
import com.skillswap.model.Notification;
import com.skillswap.model.User;
import com.skillswap.repo.NotificationRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.skillswap.dto.Dtos.NotificationDto;

@Service
public class NotificationService {
    private final NotificationRepository repo;
    private final SimpMessagingTemplate ws;

    public NotificationService(NotificationRepository repo, SimpMessagingTemplate ws) {
        this.repo = repo;
        this.ws = ws;
    }

    public void notify(User user, Enums.NotificationType type, String message, String link) {
        Notification n = repo.save(new Notification(user, type, message, link));
        // Optional real-time push (WebSocket/STOMP). Clients that don't subscribe simply poll.
        try {
            ws.convertAndSend("/topic/notifications/" + user.getId(), NotificationDto.of(n));
        } catch (Exception ignored) { }
    }
}
