package com.dbt.chatease.Repository;

import com.dbt.chatease.Entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, ChatSession.ChatSessionId> {
    
    /**
     * Find all sessions for a user, ordered by last receive time descending.
     */
    List<ChatSession> findByUserIdOrderByLastReceiveTimeDesc(String userId);
    
    /**
     * Find a specific session by user ID and contact ID.
     */
    ChatSession findByUserIdAndContactId(String userId, String contactId);

    List<ChatSession> findByUserId(String userId);

    List<ChatSession> findByContactIdAndUserIdIn(String contactId, List<String> userIds);

    List<ChatSession> findByContactTypeAndUserIdIn(Integer contactType, List<String> userIds);
}