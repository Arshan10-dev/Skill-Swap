package com.skillswap.service;

import com.skillswap.dto.Dtos.*;
import com.skillswap.model.*;
import com.skillswap.repo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {
    private final ReviewRepository reviews;
    private final SkillSessionRepository sessions;
    private final ReputationService reputation;
    private final NotificationService notifications;
    private final UserLookup lookup;

    public ReviewService(ReviewRepository reviews, SkillSessionRepository sessions, ReputationService reputation,
                         NotificationService notifications, UserLookup lookup) {
        this.reviews = reviews;
        this.sessions = sessions;
        this.reputation = reputation;
        this.notifications = notifications;
        this.lookup = lookup;
    }

    @Transactional
    public ReviewDto create(Long me, ReviewCreate in) {
        SkillSession s = sessions.findById(in.sessionId()).orElseThrow(() -> ApiException.notFound("Session"));
        if (!s.getSwapRequest().involves(me)) throw ApiException.forbidden("Not your session");
        if (s.getStatus() != Enums.SessionStatus.COMPLETED) throw ApiException.bad("You can review only completed sessions");
        if (reviews.existsBySessionIdAndReviewerId(s.getId(), me)) throw ApiException.conflict("You already reviewed this session");
        User reviewer = lookup.get(me);
        User reviewee = s.getSwapRequest().other(me);
        Review r = new Review();
        r.setSession(s);
        r.setReviewer(reviewer);
        r.setReviewee(reviewee);
        r.setRating(in.rating());
        r.setComment(in.comment());
        reviews.saveAndFlush(r);
        reputation.addPoints(reviewee, in.rating() * 4);
        reputation.addPoints(reviewer, ReputationService.POINTS_WRITE_REVIEW);
        notifications.notify(reviewee, Enums.NotificationType.REVIEW,
                reviewer.getName() + " rated you " + in.rating() + "/5", "/profile");
        return toDto(r);
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> forUser(Long userId) {
        return reviews.findByRevieweeIdOrderByCreatedAtDesc(userId).stream().map(this::toDto).toList();
    }

    ReviewDto toDto(Review r) {
        return new ReviewDto(r.getId(), r.getSession().getId(), r.getSession().getTitle(), lookup.pub(r.getReviewer()),
                r.getRating(), r.getComment(), r.getCreatedAt());
    }
}
