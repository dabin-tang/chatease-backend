package com.dbt.chatease.Service;

import com.dbt.chatease.DTO.UserApplyDTO;
import com.dbt.chatease.Utils.Result;

public interface UserApplyService {


    Result sendRequest(UserApplyDTO userApplyDTO);

    Result getReceivedFriendRequests(Integer page, Integer pageSize);

    Result getReceivedGroupRequests(Integer page, Integer pageSize);

    Result processApplyRequest(Integer applyId, Integer status);
}