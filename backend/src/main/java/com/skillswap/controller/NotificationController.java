package com.skillswap.controller;

import com.skillswap.dto.Dtos.NotificationDto;
import com.skillswap.repo.NotificationRepository;
import com.skillswap.security.CurrentUser;
import com.skillswap.service.ApiException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationRepository repo;
    public NotificationController(NotificationRepository repo) { this.repo = repo; }

    @GetMapping
    public List<NotificationDto> list() {
        return repo.findTop50ByUserIdOrderByCreatedAtDesc(CurrentUser.id()).stream().map(NotificationDto::of).toList();
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unread() { return Map.of("count", repo.countByUserIdAndReadFalse(CurrentUser.id())); }

    @PostMapping("/{id}/read")
    @Transactional
    public void read(@PathVariable Long id) {
        var n = repo.findById(id).orElseThrow(() -> ApiException.notFound("Notification"));
        if (!n.getUser().getId().equals(CurrentUser.id())) throw ApiException.forbidden("Not yours");
        n.setRead(true);
    }

    @PostMapping("/read-all")
    public void readAll() { repo.markAllRead(CurrentUser.id()); }
}
