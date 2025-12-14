package com.dbt.chatease.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "AdminLoginDTO", description = "Data Transfer Object for admin login")
public class AdminLoginDTO {

    @Schema(description = "Admin username", example = "admin")
    private String username;

    @Schema(description = "Admin password", example = "123456")
    private String password;
}