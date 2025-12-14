package com.dbt.chatease.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sys_broadcast")
public class SysBroadcast {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "broadcast_id")
    private Long broadcastId;

    @Column(name = "sender_id", length = 12, nullable = false)
    private String senderId;

    @Column(name = "content", length = 1000, nullable = false)
    private String content;

    /**
     * Message Type: 0-Text, 1-Image, 4-Video, etc.
     */
    @Column(name = "message_type")
    private Integer messageType;

    /**
     * File URL (for Image/Video messages)
     */
    @Column(name = "file_path", length = 255)
    private String filePath;

    @Column(name = "create_time")
    private LocalDateTime createTime;
}