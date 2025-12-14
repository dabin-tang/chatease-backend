package com.dbt.chatease.VO;

import com.dbt.chatease.Entity.UserApply;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(name = "FriendRequestVO", description = "Friend request view object with applicant info")
public class FriendRequestVO {
    
    @Schema(description = "Application information")
    private UserApply userApply;
    
    @Schema(description = "Applicant nickname", example = "John")
    private String applyUserNickName;
    
    @Schema(description = "Applicant avatar URL", example = "http://example.com/avatar.jpg")
    private String applyUserAvatar;
}