package com.dbt.chatease.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Schema(name = "UserApplyDTO", description = "User application data transfer object")
public class UserApplyDTO {

//    @Schema(description = "Applicant user ID", example = "USR123456789")
//    private String applyUserId;

    @Schema(description = "Receiver user ID", example = "USR987654321")
    private String receiveUserId;

//    @Schema(description = "Contact type: 0-Friend, 1-Group", example = "0")
//    private Integer contactType;

    @Schema(description = "Contact or group ID", example = "GRP123456789")
    private String contactId;

    @Schema(description = "Application information", example = "Hello, please accept my friend request!")
    private String applyInfo;
}