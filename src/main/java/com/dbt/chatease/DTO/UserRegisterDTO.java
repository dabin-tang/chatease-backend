package com.dbt.chatease.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UserRegisterDTO", description = "Data Transfer Object containing basic user registration details")
public class UserRegisterDTO {
    @Schema(description = "Email", example = "user@example.com")
    private String email;

    @Schema(description = "Nickname", example = "John")
    private String nickName;

    @Schema(description = "Password", example = "abcd123")
    private String password;

    @Schema(description = "Verification code", example = "123456")
    private String code;
}
