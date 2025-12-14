package com.dbt.chatease.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Schema(name = "UserInfoVO", description = "User information view object")
public class UserInfoVO {
    
    @Schema(description = "User ID", example = "UID123456789")
    private String userId;

    @Schema(description = "Nickname", example = "John")
    private String nickName;

    @Schema(description = "User avatar URL", example = "http://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "Gender: 0-Female, 1-Male", example = "1")
    private Integer sex;

    @Schema(description = "Profile signature", example = "Hello World!")
    private String personalSignature;

    @Schema(description = "Area name", example = "Beijing")
    private String areaName;

    @Schema(description = "Is friend: true-friend, false-not friend", example = "true")
    private Boolean isFriend;
}