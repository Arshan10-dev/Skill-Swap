package com.skillswap.repo;

import com.skillswap.model.Enums;
import com.skillswap.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findAllByOrderByCreatedAtDesc();
    List<Report> findByStatusOrderByCreatedAtDesc(Enums.ReportStatus status);
    long countByStatus(Enums.ReportStatus status);
    long countByReportedId(Long userId);
}
