package com.dbt.chatease.Utils;

import com.dbt.chatease.DTO.UserInfoDTO;

public class UserContext {
    private static final ThreadLocal<UserInfoDTO> USER_CONTEXT = new ThreadLocal<>();

    //Set current user to ThreadLocal
    public static void setUser(UserInfoDTO userInfoDTO) {
        USER_CONTEXT.set(userInfoDTO);
    }

    //Get current user from ThreadLocal
    public static UserInfoDTO getCurrentUser() {
        return USER_CONTEXT.get();
    }

    //Get current userId
    public static String getCurrentUserId() {
        UserInfoDTO user = USER_CONTEXT.get();
        return user != null ? user.getUserId() : null;
    }

    //Get current user email
    public static String getCurrentUserEmail() {
        UserInfoDTO user = USER_CONTEXT.get();
        return user != null ? user.getEmail() : null;
    }

    public static void clear() {
        USER_CONTEXT.remove();
    }
}