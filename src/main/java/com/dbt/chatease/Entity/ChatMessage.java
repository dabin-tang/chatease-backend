package com.dbt.chatease.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Entity
@Table(name = "chat_message")
@Accessors(chain = true)
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;

    /**
     * Session ID.
     */
    @Column(name = "session_id", length = 32, nullable = false)
    private String sessionId;

    /**
     * Sender User ID
     */
    @Column(name = "send_user_id", length = 32, nullable = false)
    private String sendUserId;

    /**
     * Redundant: Sender Nickname
     */
    @Column(name = "send_user_nick_name", length = 20)
    private String sendUserNickName;

    /**
     * Redundant: Sender Avatar
     */
    @Column(name = "send_user_avatar", length = 255)
    private String sendUserAvatar;

    /**
     * Receiver ID (Target User ID or Group ID)
     */
    @Column(name = "contact_id", length = 32, nullable = false)
    private String contactId;

    /**
     * Contact Type: 0-Personal, 1-Group
     */
    @Column(name = "contact_type", nullable = false)
    private Integer contactType;

    /**
     * Message Content (Text)
     */
    @Column(name = "content", length = 1000)
    private String content;

    /**
     * Message Type:
     * 0: Text
     * 1: Image (Legacy, URL in content)
     * 2: File
     * 5: System Notification
     * 6: Mixed (Text + Image) :Text in 'content', Image URL in 'filePath'
     */
    @Column(name = "message_type")
    private Integer messageType;

    /**
     * File Size
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * File Name
     */
    @Column(name = "file_name", length = 200)
    private String fileName;

    /**
     * File Path or Image URL
     */
    @Column(name = "file_path", length = 255)
    private String filePath;

    /**
     * Status: 0-Sending, 1-Sent, 2-Read, 3-Recall
     */
    @Column(name = "status")
    private Integer status;

    /**
     * Send Timestamp
     */
    @Column(name = "send_time")
    private Long sendTime;
}