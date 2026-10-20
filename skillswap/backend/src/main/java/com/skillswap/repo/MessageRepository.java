package com.skillswap.repo;

import com.skillswap.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findBySwapRequestIdOrderBySentAtAsc(Long requestId);
    List<Message> findBySwapRequestIdAndIdGreaterThanOrderBySentAtAsc(Long requestId, Long afterId);

    @Query("select count(m) from Message m where m.swapRequest.id = :rid and m.sender.id <> :uid and m.readByRecipient = false")
    long countUnread(Long rid, Long uid);
}
