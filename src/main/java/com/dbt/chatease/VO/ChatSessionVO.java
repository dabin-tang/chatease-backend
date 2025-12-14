package com.dbt.chatease.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "ChatSessionVO", description = "Chat session view object for the main list")
public class ChatSessionVO {

    @Schema(description = "Session ID")
    private String sessionId;

    @Schema(description = "Contact ID (Friend ID or Group ID)")
    private String contactId;

    @Schema(description = "Contact Name (Friend nickname or Group name)")
    private String contactName;

    @Schema(description = "Contact Avatar")
    private String contactAvatar;

    @Schema(description = "Content of the last message")
    private String lastMessage;

    @Schema(description = "Time of the last message")
    private Long lastReceiveTime;

    @Schema(description = "Unread message count")
    private Integer unreadCount;

    @Schema(description = "Contact Type: 0-Personal, 1-Group")
    private Integer contactType;
}