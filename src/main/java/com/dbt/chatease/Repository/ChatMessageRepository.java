package com.dbt.chatease.Repository;

import com.dbt.chatease.Entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * Find chat history by session ID, ordered by time ascending.
     */
    List<ChatMessage> findBySessionIdOrderBySendTimeAsc(String sessionId);

    /**
     * First load: Get the latest 15 messages for a session.
     * Ordered by MessageId DESC to get the newest ones first.
     */
    List<ChatMessage> findTop15BySessionIdOrderByMessageIdDesc(String sessionId);

    /**
     * Scroll up: Get 15 older messages before a specific message ID.
     * Used for cursor-based pagination.
     */
    List<ChatMessage> findTop15BySessionIdAndMessageIdLessThanOrderByMessageIdDesc(String sessionId, Long messageId);

    @Modifying
    void deleteBySessionId(String sessionId);
}