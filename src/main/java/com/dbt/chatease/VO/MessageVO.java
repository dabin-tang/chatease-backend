package com.dbt.chatease.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "MessageVO", description = "Message details view object")
public class MessageVO {

    @Schema(description = "Message ID")
    private Long messageId;

    @Schema(description = "Sender ID")
    private String sendUserId;

    @Schema(description = "Sender Nickname (Optional, useful for groups)")
    private String sendUserNickName;

    @Schema(description = "Sender Avatar")
    private String sendUserAvatar;

    @Schema(description = "Message Content")
    private String content;

    @Schema(description = "Message Type: 0-Text, 1-Image, etc.")
    private Integer messageType;

    @Schema(description = "File Path (if image/video)")
    private String filePath;

    @Schema(description = "Send Timestamp")
    private Long sendTime;

    @Schema(description = "Is sent by current user")
    private Boolean isMe;

    @Schema(description = "File Name (if applicable)")
    private String fileName;

    @Schema(description = "File Size in bytes (if applicable)")
    private Long fileSize;

}