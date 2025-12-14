package com.dbt.chatease.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "RobotConfigDTO", description = "DTO for updating robot configuration")
public class RobotConfigDTO {
    @Schema(description = "Robot Nickname", example = "ChatEase Helper")
    private String nickname;

    @Schema(description = "Robot Avatar URL", example = "https://example.com/robot.png")
    private String avatar;

    @Schema(description = "Welcome Message", example = "Welcome to ChatEase!")
    private String welcomeMessage;
}