package com.skillswap.service;

import com.skillswap.model.Badge;
import com.skillswap.model.Enums;
import com.skillswap.model.User;
import com.skillswap.repo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reputation points:
 *   +20 per completed session (both participants)
 *   +rating*4 for every review received (5 stars = +20, 1 star = +4)
 *   +5 for writing a review
 * Badges are re-evaluated after each of these events.
 */
@Service
public class ReputationService {
    public static final int POINTS_SESSION = 20;
    public static final int POINTS_WRITE_REVIEW = 5;

    private final UserRepository users;
    private final BadgeRepository badges;
    private final SkillSessionRepository sessions;
    private final ReviewRepository reviews;
    private final UserSkillRepository userSkills;
    private final NotificationService notifications;

    public ReputationService(UserRepository users, BadgeRepository badges, SkillSessionRepository sessions,
                             ReviewRepository reviews, UserSkillRepository userSkills,
                             NotificationService notifications) {
        this.users = users;
        this.badges = badges;
        this.sessions = sessions;
        this.reviews = reviews;
        this.userSkills = userSkills;
        this.notifications = notifications;
    }

    @Transactional
    public void addPoints(User u, int points) {
        u.setReputation(u.getReputation() + points);
        users.save(u);
        evaluateBadges(u);
    }

    @Transactional
    public void evaluateBadges(User u) {
        Long id = u.getId();
        long completed = sessions.countCompletedForUser(id);
        long fiveStars = reviews.countByRevieweeIdAndRating(id, 5);
        long written = reviews.countByReviewerId(id);
        Double avg = reviews.averageFor(id);
        long teachCount = userSkills.findByUserIdAndType(id, Enums.SkillType.TEACH).size();
        long received = reviews.findByRevieweeIdOrderByCreatedAtDesc(id).size();

        award(u, completed >= 1, "FIRST_SWAP", "First Swap", "Completed your first skill-exchange session");
        award(u, completed >= 5, "REGULAR", "Regular Swapper", "Completed 5 sessions");
        award(u, completed >= 20, "VETERAN", "Swap Veteran", "Completed 20 sessions");
        award(u, fiveStars >= 3, "FIVE_STAR", "Five-Star Mentor", "Received three 5-star reviews");
        award(u, received >= 5 && avg != null && avg >= 4.5, "TOP_RATED", "Top Rated", "Average rating of 4.5+ across 5 reviews");
        award(u, teachCount >= 3, "MULTI_TALENT", "Multi-Talented", "Offers 3 or more skills to teach");
        award(u, written >= 5, "FEEDBACK_HERO", "Feedback Hero", "Wrote 5 reviews for partners");
        award(u, u.getReputation() >= 250, "TRUSTED", "Trusted Member", "Reached 250 reputation points");
    }

    private void award(User u, boolean condition, String code, String name, String desc) {
        if (!condition || badges.existsByUserIdAndCode(u.getId(), code)) return;
        badges.save(new Badge(u, code, name, desc));
        notifications.notify(u, Enums.NotificationType.BADGE, "You earned the \"" + name + "\" badge!", "/profile");
    }
}
