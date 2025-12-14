package com.dbt.chatease.DTO;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UserLoginDTO", description = "Data Transfer Object containing basic user login information")
public class UserLoginDTO {
    @Schema(description = "Email", example = "user@example.com")
    private String email;

    @Schema(description = "Password", example = "abcd123")
    private String password;

    @Schema(description = "Verification code", example = "123456")
    private String code;
}
