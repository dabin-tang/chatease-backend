package com.dbt.chatease.VO;

import com.dbt.chatease.Entity.UserApply;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(name = "FriendRequestVO", description = "Friend request view object with applicant info")
public class FriendRequestVO {
    @Schema(description = "Application ID")
    private Integer applyId;

    @Schema(description = "Applicant User ID")
    private String applicantId;

    @Schema(description = "Applicant nickname")
    private String applicantName;

    @Schema(description = "Applicant avatar URL")
    private String applicantAvatar;

    @Schema(description = "Target User ID")
    private String targetId;

    @Schema(description = "Application message")
    private String applyInfo;

    @Schema(description = "Status: 0-Pending, 1-Accepted, 2-Rejected")
    private Integer status;

    @Schema(description = "Application Type: 0-Friend, 1-Group")
    private Integer type;

    @Schema(description = "Create Time")
    private Long createTime;

}