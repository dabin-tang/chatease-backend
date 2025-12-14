package com.dbt.chatease.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
@Schema(name = "UserInfoUpdateDTO", description = "Data transfer object for updating user information")
public class UserInfoUpdateDTO {

    @Schema(description = "Nickname", example = "John")
    @Size(max = 20, message = "Nickname cannot exceed 20 characters")
    private String nickName;

    @Schema(description = "Avatar URL", example = "http://example.com/avatar.jpg")
    @Size(max = 255, message = "Avatar URL cannot exceed 255 characters")
    private String avatar;

    @Schema(description = "Join type: 0-Direct join, 1-Join after approval", example = "0")
    private Integer joinType;

    @Schema(description = "Gender: 0-Female, 1-Male", example = "1")
    private Integer sex;

    @Schema(description = "Profile signature", example = "Hello World!")
    @Size(max = 50, message = "Personal signature cannot exceed 50 characters")
    private String personalSignature;

    @Schema(description = "Area name", example = "Beijing")
    @Size(max = 50, message = "Area name cannot exceed 50 characters")
    private String areaName;

    @Schema(description = "Area code", example = "010")
    @Size(max = 50, message = "Area code cannot exceed 50 characters")
    private String areaCode;
}