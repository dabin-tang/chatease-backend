package com.dbt.chatease.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "UserPasswordResetDTO", description = "Data transfer object for password reset")
public class UserPasswordResetDTO {
    @Schema(description = "Email address", example = "user@example.com")
    private String email;

    @Schema(description = "Verification code", example = "123456")
    private String code;

    @Schema(description = "New Password", example = "newPassword123")
    private String newPassword;
}