package com.dbt.chatease.VO;

import com.dbt.chatease.Entity.UserContact;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ContactVO", description = "Contact information view object")
public class ContactVO {
    @Schema(description = "User contact relationship information")
    private UserContact userContact;

    @Schema(description = "Contact nickname", example = "John")
    private String nickName;

    @Schema(description = "Contact avatar URL", example = "http://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "Group name", example = "Development Team")
    private String groupName;

    @Schema(description = "Group avatar URL", example = "http://example.com/group-avatar.jpg")
    private String groupAvatar;
}