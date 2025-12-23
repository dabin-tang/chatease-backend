package com.dbt.chatease.DTO;

import com.dbt.chatease.Entity.UserContact;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Schema(name = "GroupMemberDTO", description = "Data Transfer Object representing a group member with user contact details")
public class GroupMemberDTO {
    private UserContact userContact;
    private String nickName;
    private String avatar;
    private Integer sex;
    @Schema(description = "Role: 0-Owner, 1-Admin, 3-Member")
    private Integer role;
    @Schema(description = "User ID of the member")
    private String userId;
    public GroupMemberDTO(UserContact userContact, String nickName, String avatar, Integer sex) {
        this.userContact = userContact;
        this.nickName = nickName;
        this.avatar = avatar;
        this.sex = sex;
    }
}
