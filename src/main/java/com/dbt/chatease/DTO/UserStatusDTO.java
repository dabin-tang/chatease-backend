package com.dbt.chatease.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "UserStatusDTO", description = "Data Transfer Object for updating user status")
public class UserStatusDTO {

    @Schema(description = "User ID", example = "UID123456789012")
    private String userId;

    @Schema(description = "User Status (0: Disabled/Banned, 1: Active)", example = "0")
    private Integer status;
}