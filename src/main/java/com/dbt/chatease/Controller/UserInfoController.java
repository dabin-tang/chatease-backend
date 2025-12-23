package com.dbt.chatease.Controller;

import com.dbt.chatease.DTO.UserInfoUpdateDTO;
import com.dbt.chatease.DTO.UserLoginDTO;
import com.dbt.chatease.DTO.UserRegisterDTO;
import com.dbt.chatease.Entity.UserInfo;
import com.dbt.chatease.Repository.UserInfoRepository;
import com.dbt.chatease.Service.UserInfoService;
import com.dbt.chatease.Utils.Constants;
import com.dbt.chatease.Utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user-info")
@Tag(name = "UserInfo Controller", description = "User Management APIs")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserInfoController {
    private final UserInfoService userInfoService;
    private final UserInfoRepository userInfoRepository;

    /**
     * Send email verification code
     *
     * @param email
     *
     */
    @Operation(summary = "Send email verification code")
    @PostMapping("/sendCode")
    public Result sendCode(@RequestParam String email) {
        log.info("send verification code to email: {}", email);
        userInfoService.sendVerificationCode(email);
        return Result.ok(Constants.CODE_SENT);
    }

    /**
     * Register user
     *
     * @param userRegisterDTO
     */
    @Operation(summary = "Register user")
    @PostMapping("/register")
    public Result register(@RequestBody UserRegisterDTO userRegisterDTO) {
        log.info("register user with email: {}", userRegisterDTO.getEmail());
        userInfoService.register(userRegisterDTO);
        return Result.ok(Constants.REGISTRATION_SUCCESS);
    }

    /**
     * User login
     *
     * @param userLoginDTO
     * @return JWT token and user info
     */
    @Operation(summary = "User login")
    @PostMapping("/login")
    public Result login(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("user login info : {}", userLoginDTO);
        return userInfoService.login(userLoginDTO);
    }

    /**
     * Get current logged-in user information
     *
     * @return user information
     */
    @Operation(summary = "Get Current User Info", description = "Get information of the currently logged-in user")
    @GetMapping("/current")
    public Result getCurrentUserInfo() {
        log.info("get current user info");
        return userInfoService.getCurrentUserInfo();
    }

    /**
     * Update current logged-in user information
     *
     * @param userInfoUpdateDTO
     * @return success or fail
     */
    @Operation(summary = "Update Current User Info", description = "Update information of the currently logged-in user")
    @PutMapping("/update")
    public Result updateCurrentUserInfo(@RequestBody UserInfoUpdateDTO userInfoUpdateDTO) {
        log.info("update current user info: {}", userInfoUpdateDTO);
        return userInfoService.updateCurrentUserInfo(userInfoUpdateDTO);
    }

    /**
     * Update current logged-in user password
     *
     * @param password new password
     * @return success or fail
     */
    @Operation(summary = "Update Current User Password", description = "Update password of the currently logged-in user")
    @PutMapping("/update-password")
    public Result updatePassword(@RequestParam String password) {
        log.info("update current user password: {}", password);
        return userInfoService.updatePassword(password);
    }

    /**
     * User logout
     *
     * @return success
     */
    @Operation(summary = "User Logout", description = "Log out the currently logged-in user")
    @PostMapping("/logout")
    public Result logOut() {
        return userInfoService.logOut();
    }

}