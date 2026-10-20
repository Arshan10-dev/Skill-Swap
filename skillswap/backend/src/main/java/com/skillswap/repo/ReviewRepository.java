package com.skillswap.repo;

import com.skillswap.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByRevieweeIdOrderByCreatedAtDesc(Long userId);
    boolean existsBySessionIdAndReviewerId(Long sessionId, Long reviewerId);
    long countByRevieweeIdAndRating(Long userId, int rating);
    long countByReviewerId(Long userId);

    @Query("select avg(r.rating) from Review r where r.reviewee.id = :u")
    Double averageFor(Long u);

    @Query("select avg(r.rating) from Review r")
    Double platformAverage();
}
