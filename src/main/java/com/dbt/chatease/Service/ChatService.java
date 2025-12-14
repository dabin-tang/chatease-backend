package com.dbt.chatease.Service;

import com.dbt.chatease.Utils.Result;

public interface ChatService {
    /**
     * Get recent chat sessions list
     */
    Result getMySessions();

    /**
     * Get chat history with a specific contact
     * @param contactId Friend ID or Group ID
     * @param contactType 0 or 1
     * @return List of MessageVO
     */
    Result getChatHistory(String contactId, Integer contactType);

    /**
     * Mark session as read (clear red dot)
     */
    Result markAsRead(String contactId);

    void sendSystemMessage(String senderId, String receiverId, Integer contactType, String content);
}