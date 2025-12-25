package com.dbt.chatease.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(name = "GroupMemberOpDTO", description = "DTO for adding or kicking group members")
public class GroupMemberOpDTO {
    @Schema(description = "Group ID", example = "GID123")
    private String groupId;

    @Schema(description = "Target User ID", example = "UID001")
    private String targetUserId;
}