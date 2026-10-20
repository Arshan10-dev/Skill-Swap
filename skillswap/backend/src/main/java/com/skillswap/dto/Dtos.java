package com.skillswap.dto;

import com.skillswap.model.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

/** Request and response payloads for the REST API. */
public final class Dtos {
    private Dtos() {}

    // ---------- Auth ----------
    public record RegisterRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6, max = 100) String password) {}

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}

    public record AuthResponse(String token, UserSummary user) {}

    // ---------- Users ----------
    public record UserSummary(Long id, String name, String email, String role, String location,
                              int reputation, Double averageRating, boolean active) {
        public static UserSummary of(User u, Double avg) {
            return new UserSummary(u.getId(), u.getName(), u.getEmail(), u.getRole().name(), u.getLocation(),
                    u.getReputation(), avg, u.isActive());
        }
    }

    /** Public view of another user: no email. */
    public record PublicUser(Long id, String name, String location, int reputation, Double averageRating) {
        public static PublicUser of(User u, Double avg) {
            return new PublicUser(u.getId(), u.getName(), u.getLocation(), u.getReputation(), avg);
        }
    }

    public record ProfileUpdate(
            @NotBlank @Size(max = 100) String name,
            @Size(max = 1000) String bio,
            @Size(max = 100) String location,
            @Size(max = 60) String availableDays,
            @Size(max = 20) String preferredTime,
            @Size(max = 20) String preferredMode) {}

    public record PasswordChange(@NotBlank String currentPassword, @NotBlank @Size(min = 6) String newPassword) {}

    public record ProfileResponse(Long id, String name, String email, String role, String bio, String location,
                                  String availableDays, String preferredTime, String preferredMode,
                                  int reputation, Double averageRating, long completedSessions, long reviewCount,
                                  LocalDateTime memberSince, List<UserSkillDto> teaches, List<UserSkillDto> learns,
                                  List<BadgeDto> badges, boolean blockedByMe) {}

    // ---------- Skills ----------
    public record CategoryDto(Long id, String name, String icon, long skillCount) {}

    public record SkillDto(Long id, String name, Long categoryId, String categoryName) {
        public static SkillDto of(Skill s) {
            return new SkillDto(s.getId(), s.getName(), s.getCategory().getId(), s.getCategory().getName());
        }
    }

    public record UserSkillDto(Long id, Long skillId, String skillName, String categoryName,
                               String type, String level, int yearsExperience) {
        public static UserSkillDto of(UserSkill us) {
            return new UserSkillDto(us.getId(), us.getSkill().getId(), us.getSkill().getName(),
                    us.getSkill().getCategory().getName(), us.getType().name(), us.getLevel().name(),
                    us.getYearsExperience());
        }
    }

    public record AddUserSkill(Long skillId, @Size(max = 80) String newSkillName, Long categoryId,
                               @NotNull Enums.SkillType type, @NotNull Enums.Level level,
                               @Min(0) @Max(60) int yearsExperience) {}

    public record SkillCreate(@NotBlank @Size(max = 80) String name, @NotNull Long categoryId) {}

    public record CategoryCreate(@NotBlank @Size(max = 80) String name, @Size(max = 30) String icon) {}

    // ---------- Matching ----------
    public record MatchDto(PublicUser user, int score, boolean mutual,
                           List<String> theyCanTeachYou, List<String> youCanTeachThem,
                           List<String> commonDays, List<String> reasons) {}

    // ---------- Requests ----------
    public record SwapRequestCreate(@NotNull Long receiverId, Long offeredSkillId, Long requestedSkillId,
                                    @Size(max = 500) String message) {}

    public record SwapRequestDto(Long id, PublicUser sender, PublicUser receiver, String offeredSkill,
                                 String requestedSkill, String message, String status, int compatibilityScore,
                                 LocalDateTime createdAt, LocalDateTime respondedAt, long unreadMessages) {}

    // ---------- Chat ----------
    public record MessageCreate(@NotBlank @Size(max = 2000) String content) {}

    public record MessageDto(Long id, Long requestId, Long senderId, String senderName, String content,
                             LocalDateTime sentAt) {
        public static MessageDto of(Message m) {
            return new MessageDto(m.getId(), m.getSwapRequest().getId(), m.getSender().getId(),
                    m.getSender().getName(), m.getContent(), m.getSentAt());
        }
    }

    // ---------- Sessions ----------
    public record SessionCreate(@NotNull Long requestId, @NotBlank @Size(max = 120) String title,
                                @NotNull @Future LocalDateTime scheduledAt,
                                @Min(15) @Max(480) int durationMinutes, Enums.SessionMode mode,
                                @Size(max = 300) String locationOrLink, @Size(max = 1000) String notes) {}

    public record SessionUpdate(LocalDateTime scheduledAt, Integer durationMinutes, Enums.SessionMode mode,
                                @Size(max = 300) String locationOrLink, @Size(max = 1000) String notes) {}

    public record SessionDto(Long id, Long requestId, String title, LocalDateTime scheduledAt, int durationMinutes,
                             String mode, String locationOrLink, String notes, String status,
                             PublicUser partner, String createdByName, LocalDateTime completedAt,
                             boolean reviewedByMe) {}

    // ---------- Reviews ----------
    public record ReviewCreate(@NotNull Long sessionId, @Min(1) @Max(5) int rating, @Size(max = 1000) String comment) {}

    public record ReviewDto(Long id, Long sessionId, String sessionTitle, PublicUser reviewer, int rating,
                            String comment, LocalDateTime createdAt) {}

    public record BadgeDto(String code, String name, String description, LocalDateTime awardedAt) {
        public static BadgeDto of(Badge b) {
            return new BadgeDto(b.getCode(), b.getName(), b.getDescription(), b.getAwardedAt());
        }
    }

    // ---------- Notifications ----------
    public record NotificationDto(Long id, String type, String message, String link, boolean read,
                                  LocalDateTime createdAt) {
        public static NotificationDto of(Notification n) {
            return new NotificationDto(n.getId(), n.getType().name(), n.getMessage(), n.getLink(), n.isRead(),
                    n.getCreatedAt());
        }
    }

    // ---------- Safety ----------
    public record ReportCreate(@NotNull Long reportedUserId, @NotBlank @Size(max = 60) String reason,
                               @Size(max = 1000) String details) {}

    public record ReportDto(Long id, PublicUser reporter, PublicUser reported, String reason, String details,
                            String status, String adminNote, LocalDateTime createdAt, LocalDateTime resolvedAt,
                            long reportsAgainstUser) {}

    public record ReportResolve(@NotNull Enums.ReportStatus status, @Size(max = 500) String adminNote,
                                boolean suspendUser) {}

    public record BlockedUserDto(Long id, String name, LocalDateTime blockedAt) {}

    // ---------- Admin ----------
    public record AnalyticsDto(long totalUsers, long activeUsers, long suspendedUsers, long newUsersLast7Days,
                               long totalSkills, long totalRequests, long pendingRequests, long acceptedRequests,
                               long completedSessions, long scheduledSessions, long openReports,
                               Double averageRating, List<NameCount> topTaughtSkills,
                               List<NameCount> topWantedSkills, List<NameCount> requestsByStatus) {}

    public record NameCount(String name, long count) {}

    public record PublicStats(long users, long skills, long completedSessions) {}
}
