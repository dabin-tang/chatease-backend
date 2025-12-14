package com.dbt.chatease.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Schema(name = "GroupInfoDTO", description = "Group information DTO")
public class GroupInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 群ID, Group ID
     */
    @Schema(description = "Group ID", example = "GRP123456789")
    private String groupId;

    /**
     * 群组名, Group name
     */
    @Schema(description = "Group name", example = "Development Team")
    private String groupName;

    /**
     * 群头像, Group avatar
     */
    @Schema(description = "Group avatar URL", example = "http://example.com/group-avatar.jpg")
    private String groupAvatar;

    /**
     * 群公告, Group announcement
     */
    @Schema(description = "Group announcement", example = "Welcome to our group! Please read the rules.")
    private String groupNotice;

    /**
     * 加入方式 0:直接加入 1:同意后加入, Join type: 0-Join directly, 1-Join after admin approval
     */
    @Schema(description = "Join type: 0-Join directly, 1-Join after admin approval", example = "0")
    private Integer joinType;
}