package com.skillswap.model;

public final class Enums {
    private Enums() {}
    public enum Role { USER, ADMIN }
    public enum SkillType { TEACH, LEARN }
    public enum Level { BEGINNER, INTERMEDIATE, EXPERT }
    public enum RequestStatus { PENDING, ACCEPTED, REJECTED, CANCELLED, COMPLETED }
    public enum SessionStatus { SCHEDULED, COMPLETED, CANCELLED }
    public enum SessionMode { ONLINE, IN_PERSON }
    public enum ReportStatus { OPEN, RESOLVED, DISMISSED }
    public enum NotificationType { REQUEST, REQUEST_UPDATE, MESSAGE, SESSION, REMINDER, REVIEW, BADGE, SYSTEM }
}
