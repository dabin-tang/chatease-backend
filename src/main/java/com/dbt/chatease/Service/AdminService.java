package com.dbt.chatease.Service;

import com.dbt.chatease.DTO.BroadcastDTO;
import com.dbt.chatease.Utils.Result;

public interface AdminService {
    Result getUserList(Integer page, Integer size, String keyword);

    Result updateUserStatus(String userId, Integer status);

    Result forceLogout(String userId);

    Result getGroupList(Integer page, Integer size, String keyword);

    Result disbandGroup(String groupOwnerId, String groupId);

    Result getSystemSettings();

    Result updateRobotConfig(com.dbt.chatease.DTO.RobotConfigDTO dto);

    Result sendBroadcast(BroadcastDTO dto);

}