package com.dbt.chatease.Utils;

public class Constants {
    private Constants() {
    }

    public static final String VERIFICATION_CODE_PREFIX = "VERIFICATION_CODE:";
    public static final String ACCOUNT_EXISTS = "Account already exists";
    public static final String CODE_SENT = "Verification code has been sent to email";
    public static final String INVALID_EMAIL = "Invalid email address format";
    public static final String REGISTRATION_SUCCESS = "Registration successful";
    public static final String INVALID_CODE = "Invalid verification code";
    public static final Integer USER_STATUS_ACTIVE = 1;
    public static final Integer USER_JOIN_TYPE_DEFAULT = 1;
    public static final String LOGIN_SUCCESS = "Login successful";
    public static final String LOGIN_FAILED = "Incorrect username or password. Please try again.";
    public static final String USER_BANNED = "This account has been suspended for violating our terms of service.";
    public static final String AVATAR_DEFAULT_URL = "https://img.ixintu.com/download/jpg/20201010/a86227130c46e8862bce5cab6ba34533_512_505.jpg!con";
    public static final String PASSWORD_TOO_SHORT = "Password must be at least 6 characters";
    public static final String PASSWORD_TOO_LONG = "Password is too long";
    public static final String GROUP_NAME_EMPTY = "Group name cannot be empty";
    public static final String GROUP_NAME_TOO_LONG = "Group name cannot exceed 20 characters";
    public static final String GROUP_JOIN_TYPE_EMPTY = "Join type cannot be empty";
    public static final String GROUP_CREATE_SUCCESS = "Group created successfully";
    public static final String UNKNOWN_ERROR = "An unknown error occurred";
    public static final String GROUP_NOT_FOUND = "Group not found";
    public static final String ONLY_GROUP_OWNER_CAN_MODIFY = "Only the group owner can modify";
    public static final String GROUP_NOTICE_TOO_LONG = "Group notice cannot exceed 500 characters";
    public static final String GROUPINFO_SUCCESS_UPDATE = "Group Info updated successfully";
    public static final String GROUP_ACCESS_DENIED = "Access to this group is denied.";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String CONTACT_ID_EMPTY = "Contact ID cannot be empty";
    public static final String NO_MATCHING_CONTACT = "No matching users or groups found";
    public static final String INVALID_CONTACT_ID = "Invalid contact ID";
    public static final String USER_HAS_BLOCKED_YOU = "The user has blocked you";
    public static final String REQUEST_SENT_SUCCESS = "Request sent successfully";
    public static final String USER_ALREADY_ADDED = "User has already been added";
    public static final String GROUP_ALREADY_ADDED = "The group has already been added";
    public static final String FRIEND_ADDED_SUCCESS = "Friend added successfully";
    public static final String GROUP_JOIN_SUCCESS = "Successfully joined the group";
    public static final String APPLICATION_NOT_FOUND = "Application not found";
    public static final String UNAUTHORIZED_OPERATION = "You are not authorized to perform this operation";
    public static final String APPLY_REQUEST_ALREADY_PROCESSED = "This application request has already been processed";
    public static final String APPLICATION_PROCESSED_SUCCESS = "Application processed successfully";
    public static final String INVALID_STATUS = "Invalid status value";
    public static final String INVALID_CONTACT_TYPE = "Invalid contactType";
    public static final String APPLICATION_ACCEPTED_SUCCESS = "Accepted";
    public static final String APPLICATION_REJECTED_SUCCESS = "Rejected";
    public static final String APPLICATION_BLOCKED_SUCCESS = "Blocked";
    public static final String CONTACT_DELETED = "Contact deleted";
    public static final String CONTACT_BLOCKED = "Contact blocked";
    public static final String USER_INFO_UPDATED = "User information updated successfully.";
    public static final String USER_INFO_UPDATE_FAILED = "Failed to update user information.";
    public static final String PASSWORD_UPDATED_SUCCESS = "Password updated successfully";
    public static final String LOGOUT_SUCCESS = "Logout successful";
    public static final String USER_STATUS_UPDATED_SUCCESS = "User status updated successfully";
    public static final String USER_NOT_FOUND_OR_UPDATE_FAILED = "User not found or update failed";
    public static final String USER_ID_AND_STATUS_CANNOT_BE_NULL = "UserId and Status cannot be null";
    public static final String GROUP_DISBANDED_SUCCESS = "Group disbanded successfully. Part on good terms.";
    public static final String GROUP_DISBAND_FAILED = "Failed to disband group";
}
