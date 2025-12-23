package com.dbt.chatease.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Schema(name = "GroupInfoDTO", description = "Group information DTO")
public class GroupInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Group ID", example = "GRP123456789")
    private String groupId;

    @Schema(description = "Group name", example = "Team")
    private String groupName;

    @Schema(description = "Group avatar URL", example = "http://example.com/group-avatar.jpg")
    private String groupAvatar;

    @Schema(description = "Group announcement", example = "Welcome to our group! ")
    private String groupNotice;

    @Schema(description = "Join type: 0-Join directly, 1-Join after admin approval", example = "0")
    private Integer joinType;

    private List<String> userIds;


}