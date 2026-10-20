package com.skillswap.repo;

import com.skillswap.model.Enums;
import com.skillswap.model.SwapRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface SwapRequestRepository extends JpaRepository<SwapRequest, Long> {
    List<SwapRequest> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);
    List<SwapRequest> findBySenderIdOrderByCreatedAtDesc(Long senderId);

    @Query("select r from SwapRequest r where (r.sender.id = :u or r.receiver.id = :u) and r.status in :statuses order by r.createdAt desc")
    List<SwapRequest> findForUserWithStatus(Long u, List<Enums.RequestStatus> statuses);

    @Query("select count(r) > 0 from SwapRequest r where ((r.sender.id = :a and r.receiver.id = :b) or (r.sender.id = :b and r.receiver.id = :a)) and r.status in :statuses")
    boolean existsBetweenWithStatus(Long a, Long b, List<Enums.RequestStatus> statuses);

    default boolean existsOpenBetween(Long a, Long b) {
        return existsBetweenWithStatus(a, b, List.of(Enums.RequestStatus.PENDING, Enums.RequestStatus.ACCEPTED));
    }

    long countByStatus(Enums.RequestStatus status);
}
