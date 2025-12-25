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
    @Schema(description = "Basic Group Info")
    private GroupInfo groupInfo;

    @Schema(description = "Group ID (Redundant for convenience)")
    private String groupId;

    @Schema(description = "Group Name")
    private String groupName;

    @Schema(description = "Group Avatar")
    private String groupAvatar;

    @Schema(description = "Group Notice")
    private String groupNotice;

    @Schema(description = "Owner ID")
    private String ownerId;

    @Schema(description = "Join type: 0-Direct, 1-Approval")
    private Integer joinType;

    @Schema(description = "Member Count")
    private Integer memberCount;

    @Schema(description = "Members List")
    private List<GroupMemberDTO> groupMemberDTOList;


    public static GroupInfoVO fromEntity(GroupInfo groupInfo, List<GroupMemberDTO> members) {
        GroupInfoVO vo = new GroupInfoVO();
        vo.setGroupInfo(groupInfo);

        if (groupInfo != null) {
            vo.setGroupId(groupInfo.getGroupId());
            vo.setGroupName(groupInfo.getGroupName());
            vo.setGroupAvatar(groupInfo.getGroupAvatar());
            vo.setGroupNotice(groupInfo.getGroupNotice());
            vo.setOwnerId(groupInfo.getGroupOwnerId());
            vo.setJoinType(groupInfo.getJoinType());
        }
        vo.setGroupMemberDTOList(members);
        vo.setMemberCount(members != null ? members.size() : 0);
        return vo;
    }

}
