package com.dbt.chatease.Service;

import com.dbt.chatease.DTO.GroupInfoDTO;
import com.dbt.chatease.DTO.GroupMemberOpDTO;
import com.dbt.chatease.Utils.Result;

public interface GroupInfoService {
    Result createGroup(GroupInfoDTO groupInfoDTO);

    Result updateGroup(GroupInfoDTO groupInfoDTO);

    Result getMyGroups();

    Result getGroupInfoByIdAndMemberCount(String groupId);

    Result getGroupInfoWithMembersByGroupId(String groupId);

    Result quitGroup(String groupId);

    Result disbandGroup(String groupId);

    Result kickMembers(GroupMemberOpDTO dto);

    Result addMembers(GroupMemberOpDTO dto);
}