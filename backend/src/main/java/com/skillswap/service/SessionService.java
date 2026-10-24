package com.skillswap.service;

import com.skillswap.dto.Dtos.*;
import com.skillswap.model.*;
import com.skillswap.repo.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SessionService {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM, hh:mm a");

    private final SkillSessionRepository sessions;
    private final SwapRequestRepository requests;
    private final ReviewRepository reviews;
    private final NotificationService notifications;
    private final ReputationService reputation;
    private final UserLookup lookup;

    public SessionService(SkillSessionRepository sessions, SwapRequestRepository requests, ReviewRepository reviews,
            NotificationService notifications, ReputationService reputation, UserLookup lookup) {
        this.sessions = sessions;
        this.requests = requests;
        this.reviews = reviews;
        this.notifications = notifications;
        this.reputation = reputation;
        this.lookup = lookup;
    }

    @Transactional
    public SessionDto schedule(Long me, SessionCreate in) {
        SwapRequest r = requests.findById(in.requestId()).orElseThrow(() -> ApiException.notFound("Request"));
        if (!r.involves(me))
            throw ApiException.forbidden("Not your exchange");
        if (r.getStatus() != Enums.RequestStatus.ACCEPTED)
            throw ApiException.bad("Sessions can be scheduled only for accepted exchanges");
        SkillSession s = new SkillSession();
        s.setSwapRequest(r);
        s.setCreatedBy(lookup.get(me));
        s.setTitle(in.title());
        s.setScheduledAt(in.scheduledAt());
        s.setDurationMinutes(in.durationMinutes() == 0 ? 60 : in.durationMinutes());
        s.setMode(in.mode() == null ? Enums.SessionMode.ONLINE : in.mode());
        s.setLocationOrLink(in.locationOrLink());
        s.setNotes(in.notes());
        sessions.save(s);
        notifications.notify(r.other(me), Enums.NotificationType.SESSION,
                s.getCreatedBy().getName() + " scheduled \"" + s.getTitle() + "\" on " + FMT.format(s.getScheduledAt()),
                "/sessions");
        return toDto(s, me);
    }

    @Transactional
    public SessionDto update(Long me, Long id, SessionUpdate in) {
        SkillSession s = mine(me, id);
        if (s.getStatus() != Enums.SessionStatus.SCHEDULED)
            throw ApiException.bad("Only scheduled sessions can be edited");
        if (in.scheduledAt() != null) {
            if (in.scheduledAt().isBefore(LocalDateTime.now()))
                throw ApiException.bad("Pick a time in the future");
            s.setScheduledAt(in.scheduledAt());
            s.setReminderSent(false);
        }
        if (in.durationMinutes() != null)
            s.setDurationMinutes(in.durationMinutes());
        if (in.mode() != null)
            s.setMode(in.mode());
        if (in.locationOrLink() != null)
            s.setLocationOrLink(in.locationOrLink());
        if (in.notes() != null)
            s.setNotes(in.notes());
        notifications.notify(s.getSwapRequest().other(me), Enums.NotificationType.SESSION,
                "\"" + s.getTitle() + "\" was rescheduled to " + FMT.format(s.getScheduledAt()), "/sessions");
        return toDto(s, me);
    }

    @Transactional
    public SessionDto complete(Long me, Long id) {
        SkillSession s = mine(me, id);
        if (s.getStatus() != Enums.SessionStatus.SCHEDULED)
            throw ApiException.bad("Session is not open");
        System.out.println("Scheduled time: " + s.getScheduledAt());
        System.out.println("Backend current time: " + LocalDateTime.now());
        if (s.getScheduledAt().isAfter(LocalDateTime.now()))
            throw ApiException.bad("A session can be marked complete only after its start time");
        s.setStatus(Enums.SessionStatus.COMPLETED);
        s.setCompletedAt(LocalDateTime.now());
        sessions.saveAndFlush(s);
        User a = s.getSwapRequest().getSender();
        User b = s.getSwapRequest().getReceiver();
        reputation.addPoints(a, ReputationService.POINTS_SESSION);
        reputation.addPoints(b, ReputationService.POINTS_SESSION);
        notifications.notify(s.getSwapRequest().other(me), Enums.NotificationType.REVIEW,
                "\"" + s.getTitle() + "\" is complete. Leave a review for your partner!", "/sessions");
        notifications.notify(lookup.get(me), Enums.NotificationType.REVIEW,
                "Session complete. Leave a review for your partner!", "/sessions");
        return toDto(s, me);
    }

    @Transactional
    public SessionDto cancel(Long me, Long id) {
        SkillSession s = mine(me, id);
        if (s.getStatus() != Enums.SessionStatus.SCHEDULED)
            throw ApiException.bad("Session is not open");
        s.setStatus(Enums.SessionStatus.CANCELLED);
        notifications.notify(s.getSwapRequest().other(me), Enums.NotificationType.SESSION,
                "\"" + s.getTitle() + "\" was cancelled", "/sessions");
        return toDto(s, me);
    }

    @Transactional(readOnly = true)
    public List<SessionDto> forUser(Long me) {
        return sessions.findForUser(me).stream().map(s -> toDto(s, me)).toList();
    }

    @Transactional(readOnly = true)
    public List<SessionDto> forRequest(Long me, Long requestId) {
        SwapRequest r = requests.findById(requestId).orElseThrow(() -> ApiException.notFound("Request"));
        if (!r.involves(me))
            throw ApiException.forbidden("Not your exchange");
        return sessions.findBySwapRequestIdOrderByScheduledAtDesc(requestId).stream().map(s -> toDto(s, me)).toList();
    }

    /**
     * Sends a reminder roughly one hour before each scheduled session. Runs every 5
     * minutes.
     */
    @Scheduled(fixedDelay = 300_000, initialDelay = 30_000)
    @Transactional
    public void sendReminders() {
        LocalDateTime now = LocalDateTime.now();
        for (SkillSession s : sessions.findByStatusAndReminderSentFalseAndScheduledAtBetween(
                Enums.SessionStatus.SCHEDULED, now, now.plusMinutes(65))) {
            String msg = "Reminder: \"" + s.getTitle() + "\" starts at " + FMT.format(s.getScheduledAt());
            notifications.notify(s.getSwapRequest().getSender(), Enums.NotificationType.REMINDER, msg, "/sessions");
            notifications.notify(s.getSwapRequest().getReceiver(), Enums.NotificationType.REMINDER, msg, "/sessions");
            s.setReminderSent(true);
        }
    }

    SkillSession mine(Long me, Long id) {
        SkillSession s = sessions.findById(id).orElseThrow(() -> ApiException.notFound("Session"));
        if (!s.getSwapRequest().involves(me))
            throw ApiException.forbidden("Not your session");
        return s;
    }

    SessionDto toDto(SkillSession s, Long me) {
        return new SessionDto(s.getId(), s.getSwapRequest().getId(), s.getTitle(), s.getScheduledAt(),
                s.getDurationMinutes(), s.getMode().name(), s.getLocationOrLink(), s.getNotes(), s.getStatus().name(),
                lookup.pub(s.getSwapRequest().other(me)), s.getCreatedBy().getName(), s.getCompletedAt(),
                reviews.existsBySessionIdAndReviewerId(s.getId(), me));
    }
}
