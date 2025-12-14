package com.dbt.chatease.Service;

import com.dbt.chatease.DTO.UserInfoUpdateDTO;
import com.dbt.chatease.DTO.UserLoginDTO;
import com.dbt.chatease.DTO.UserRegisterDTO;
import com.dbt.chatease.Entity.UserInfo;
import com.dbt.chatease.Utils.Result;

public interface UserInfoService {
    /**
     *Send email verification code
     * @param email
     */
    void sendVerificationCode(String email);

    /**
     *  Register user
     * @param userRegisterDTO
     */
    void register(UserRegisterDTO userRegisterDTO);

    Result login(UserLoginDTO userLoginDTO);

    Result getCurrentUserInfo();

    Result updateCurrentUserInfo(UserInfoUpdateDTO userInfoUpdateDTO);

    Result updatePassword(String password);

    Result logOut();
}