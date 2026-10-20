package com.skillswap.repo;

import com.skillswap.model.Enums;
import com.skillswap.model.SkillSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

public interface SkillSessionRepository extends JpaRepository<SkillSession, Long> {
    @Query("select s from SkillSession s where s.swapRequest.sender.id = :u or s.swapRequest.receiver.id = :u order by s.scheduledAt desc")
    List<SkillSession> findForUser(Long u);

    List<SkillSession> findBySwapRequestIdOrderByScheduledAtDesc(Long requestId);

    List<SkillSession> findByStatusAndReminderSentFalseAndScheduledAtBetween(Enums.SessionStatus status, LocalDateTime from, LocalDateTime to);

    @Query("select count(s) from SkillSession s where s.status = :status and (s.swapRequest.sender.id = :u or s.swapRequest.receiver.id = :u)")
    long countForUserWithStatus(Long u, Enums.SessionStatus status);

    default long countCompletedForUser(Long u) { return countForUserWithStatus(u, Enums.SessionStatus.COMPLETED); }

    long countByStatus(Enums.SessionStatus status);
}
