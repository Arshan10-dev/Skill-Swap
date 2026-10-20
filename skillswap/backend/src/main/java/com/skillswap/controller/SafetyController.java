package com.skillswap.controller;

import com.skillswap.dto.Dtos.*;
import com.skillswap.model.Block;
import com.skillswap.model.Enums;
import com.skillswap.model.Report;
import com.skillswap.model.User;
import com.skillswap.repo.BlockRepository;
import com.skillswap.repo.ReportRepository;
import com.skillswap.repo.UserRepository;
import com.skillswap.security.CurrentUser;
import com.skillswap.service.ApiException;
import com.skillswap.service.NotificationService;
import com.skillswap.service.UserLookup;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SafetyController {
    private final ReportRepository reports;
    private final BlockRepository blocks;
    private final UserRepository users;
    private final NotificationService notifications;
    private final UserLookup lookup;

    public SafetyController(ReportRepository reports, BlockRepository blocks, UserRepository users,
                            NotificationService notifications, UserLookup lookup) {
        this.reports = reports;
        this.blocks = blocks;
        this.users = users;
        this.notifications = notifications;
        this.lookup = lookup;
    }

    @PostMapping("/reports")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public void report(@Valid @RequestBody ReportCreate in) {
        Long me = CurrentUser.id();
        if (in.reportedUserId().equals(me)) throw ApiException.bad("You cannot report yourself");
        Report r = new Report();
        r.setReporter(lookup.get(me));
        r.setReported(lookup.get(in.reportedUserId()));
        r.setReason(in.reason());
        r.setDetails(in.details());
        reports.save(r);
        for (User admin : users.findByRoleAndActiveTrue(Enums.Role.ADMIN)) {
            notifications.notify(admin, Enums.NotificationType.SYSTEM,
                    "New report against " + r.getReported().getName() + ": " + r.getReason(), "/admin/reports");
        }
    }

    @GetMapping("/blocks")
    public List<BlockedUserDto> blocked() {
        return blocks.findByBlockerId(CurrentUser.id()).stream()
                .map(b -> new BlockedUserDto(b.getBlocked().getId(), b.getBlocked().getName(), b.getCreatedAt())).toList();
    }

    @PostMapping("/blocks/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public void block(@PathVariable Long userId) {
        Long me = CurrentUser.id();
        if (userId.equals(me)) throw ApiException.bad("You cannot block yourself");
        if (blocks.findByBlockerIdAndBlockedId(me, userId).isPresent()) return;
        blocks.save(new Block(lookup.get(me), lookup.get(userId)));
    }

    @DeleteMapping("/blocks/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void unblock(@PathVariable Long userId) {
        blocks.findByBlockerIdAndBlockedId(CurrentUser.id(), userId).ifPresent(blocks::delete);
    }
}
