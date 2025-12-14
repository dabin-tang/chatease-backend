package com.dbt.chatease.DTO;

import lombok.Data;
import lombok.experimental.Accessors;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Accessors(chain = true)
@Schema(name = "UserInfoDTO", description = "basic user information")
public class UserInfoDTO {
    
    @Schema(description = "userId", example = "123456789012")
    private String userId;

    @Schema(description = "email", example = "user@example.com")
    private String email;

    @Schema(description = "nickName", example = "John")
    private String nickName;

    @Schema(description = "avatar url", example = "http://example.com/avatar.jpg")
    private String avatar;

}