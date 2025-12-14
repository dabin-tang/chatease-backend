package com.dbt.chatease.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import java.io.Serializable;

@Data
@Entity
@Table(name = "chat_session")
@Accessors(chain = true)
@IdClass(ChatSession.ChatSessionId.class)
public class ChatSession {

    /**
     * Owner User ID
     */
    @Id
    @Column(name = "user_id", length = 12, nullable = false)
    private String userId;

    /**
     * Contact ID (Friend ID or Group ID)
     */
    @Id
    @Column(name = "contact_id", length = 12, nullable = false)
    private String contactId;

    /**
     * Associated Session ID
     */
    @Column(name = "session_id", length = 32, nullable = false)
    private String sessionId;

    /**
     * Contact Name (Redundant)
     */
    @Column(name = "contact_name", length = 20)
    private String contactName;

    /**
     * Contact Avatar (Redundant)
     */
    @Column(name = "contact_avatar", length = 255)
    private String contactAvatar;

    /**
     * Last Message Snapshot
     */
    @Column(name = "last_message", length = 500)
    private String lastMessage;

    /**
     * Last Receive Time
     */
    @Column(name = "last_receive_time")
    private Long lastReceiveTime;

    /**
     * Unread Message Count
     */
    @Column(name = "unread_count")
    private Integer unreadCount;

    /**
     * Contact Type: 0-Personal, 1-Group
     */
    @Column(name = "contact_type")
    private Integer contactType;

    /**
     * Composite Key Class
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatSessionId implements Serializable {
        private String userId;
        private String contactId;
    }
}