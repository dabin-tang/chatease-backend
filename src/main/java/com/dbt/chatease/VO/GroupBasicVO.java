package com.dbt.chatease.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Schema(name = "GroupBasicVO", description = "Basic group information view object")
public class GroupBasicVO {
    
    @Schema(description = "Group ID", example = "GID123456789")
    private String groupId;

    @Schema(description = "Group name", example = "Development Team")
    private String groupName;

    @Schema(description = "Group avatar URL", example = "http://example.com/group-avatar.jpg")
    private String groupAvatar;

    @Schema(description = "Creation time", example = "2025-09-06T10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "Join type: 0-Join directly, 1-Join after admin approval", example = "0")
    private Integer joinType;

    @Schema(description = "Group owner ID", example = "USR123456789")
    private String groupOwnerId;

    @Schema(description = "Is member: true-already joined, false-not joined", example = "true")
    private Boolean isMember;
}