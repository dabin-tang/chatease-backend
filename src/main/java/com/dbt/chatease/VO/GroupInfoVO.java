package com.dbt.chatease.VO;

import com.dbt.chatease.DTO.GroupMemberDTO;
import com.dbt.chatease.Entity.GroupInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Schema(name = "GroupInfoVO", description = "View Object containing group information and its members' details")
public class GroupInfoVO {
    @Schema(description = "Group information")
    private GroupInfo groupInfo;
    @Schema(description = "Group members basic information list and other details")
    private List<GroupMemberDTO> groupMemberDTOList;
}
