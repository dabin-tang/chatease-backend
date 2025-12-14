package com.dbt.chatease.VO;

import com.dbt.chatease.Entity.GroupInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

@Data
@Schema(name = "GroupListVO", description = "Group List View Object for Admin Dashboard")
public class GroupListVO extends GroupInfo {

    @Schema(description = "Group Owner Nickname", example = "Jack Ma")
    private String groupOwnerName;

    @Schema(description = "Group Member Count", example = "500")
    private Long memberCount;

    public static GroupListVO fromEntity(GroupInfo groupInfo, String ownerName, Long memberCount) {
        GroupListVO vo = new GroupListVO();
        BeanUtils.copyProperties(groupInfo, vo);
        vo.setGroupOwnerName(ownerName);
        vo.setMemberCount(memberCount);
        return vo;
    }
}