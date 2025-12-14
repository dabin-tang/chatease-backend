package com.dbt.chatease.Repository;

import com.dbt.chatease.Entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    /**
     * Find chat history by session ID, ordered by time ascending.
     */
    List<ChatMessage> findBySessionIdOrderBySendTimeAsc(String sessionId);
}