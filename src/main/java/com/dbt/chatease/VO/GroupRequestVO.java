package com.dbt.chatease.VO;

import com.dbt.chatease.Entity.UserApply;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(name = "GroupRequestVO", description = "Group request view object with applicant and group info")
public class GroupRequestVO {
    @Schema(description = "Application ID")
    private Integer applyId;

    @Schema(description = "Applicant User ID")
    private String applicantId;

    @Schema(description = "Applicant nickname")
    private String applicantName;

    @Schema(description = "Applicant avatar URL")
    private String applicantAvatar;

    @Schema(description = "Group ID (Target ID)")
    private String groupId;

    @Schema(description = "Group name")
    private String groupName;

    @Schema(description = "Group avatar URL")
    private String groupAvatar;

    @Schema(description = "Application message")
    private String applyInfo;

    @Schema(description = "Status: 0-Pending, 1-Accepted, 2-Rejected")
    private Integer status;

    @Schema(description = "Application Type: 1-Group")
    private Integer type;

    @Schema(description = "Create Time")
    private Long createTime;
}