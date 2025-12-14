package com.dbt.chatease.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "BroadcastDTO", description = "Data Transfer Object for sending system broadcast")
public class BroadcastDTO {
    
    @Schema(description = "Text content or caption", example = "Happy New Year!")
    private String content;

    @Schema(description = "Message Type: 0-Text, 1-Image, 4-Video", example = "0")
    private Integer messageType;

    @Schema(description = "File URL (Uploaded via UploadController)", example = "/files/chateaseimg/2023/11/29/a.jpg")
    private String filePath;
}