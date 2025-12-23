package com.dbt.chatease.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Schema(name = "UserApplyDTO", description = "User application data transfer object")
public class UserApplyDTO {
    @Schema(description = "Target ID (User ID or Group ID)", example = "USR123456789")
    private String contactId;

    @Schema(description = "Application information", example = "Hello, please accept my friend request!")
    private String applyInfo;

    @Schema(description = "Type: 1-Friend, 2-Group")
    private Integer type;

}