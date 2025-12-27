package com.dbt.chatease.Controller;

import com.dbt.chatease.DTO.AdminLoginDTO;
import com.dbt.chatease.DTO.RobotConfigDTO;
import com.dbt.chatease.DTO.UserStatusDTO;
import com.dbt.chatease.Entity.AdminInfo;
import com.dbt.chatease.Entity.GroupInfo;
import com.dbt.chatease.Repository.AdminInfoRepository;
import com.dbt.chatease.Repository.GroupInfoRepository;
import com.dbt.chatease.Service.AdminService;
import com.dbt.chatease.Utils.Constants;
import com.dbt.chatease.Utils.JwtUtil;
import com.dbt.chatease.Utils.PasswordUtil;
import com.dbt.chatease.Utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Controller", description = "Administrator Management APIs")
public class AdminController {
    private final AdminInfoRepository adminInfoRepository;
    private final JwtUtil jwtUtil;
    private final AdminService adminService;
    private final GroupInfoRepository groupInfoRepository;

    /**
     * Admin login
     *
     * @return JWT token and admin info
     */
    @PostMapping("/login")
    @Operation(summary = "Admin Login", description = "Authenticate admin and return JWT token")
    public Result login(@RequestBody AdminLoginDTO adminLoginDTO) {
        String username = adminLoginDTO.getUsername();
        String password = adminLoginDTO.getPassword();

        log.info("Admin login attempt: {}", username);

        //Find admin by username
        AdminInfo admin = adminInfoRepository.findByUsername(username);
        if (admin == null) {
            return Result.fail("Admin account not found");
        }

        //Verify password
        if (!PasswordUtil.checkPassword(password, admin.getPassword())) {
            return Result.fail("Incorrect password");
        }

        //Update last login time
        admin.setLastLoginTime(LocalDateTime.now());
        adminInfoRepository.save(admin);

        //Generate admin-specific token
        String token = jwtUtil.generateAdminToken(admin);

        //Build response
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("adminName", admin.getUsername());
        data.put("role", admin.getRole());

        return Result.ok(data);
    }

    /**
     * Get paginated list of users with optional search keyword
     *
     * @return user list and some other info
     */
    @GetMapping("/users")
    @Operation(summary = "Get User List", description = "Retrieve a paginated list of users with optional search keyword")
    public Result getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        return adminService.getUserList(page, size, keyword);
    }

    /**
     * Update user status (e.g., Ban or Unban user)
     *
     * @return success or fail
     */
    @PutMapping("/user/status")
    @Operation(summary = "Update User Status", description = "Change user status (e.g., Ban or Unban user)")
    public Result updateUserStatus(@RequestBody UserStatusDTO userStatusDTO) {
        log.info("Update User Status: {}", userStatusDTO);
        return adminService.updateUserStatus(userStatusDTO.getUserId(), userStatusDTO.getStatus());
    }

    /**
     * Force logout a user by user ID
     *
     * @return success or fail
     */
    @PostMapping("/user/force-logout")
    @Operation(summary = "Force Logout User", description = "Force logout a user by user ID")
    public Result forceLogout(String userId) {
        log.info("Force Logout User: {}", userId);
        return adminService.forceLogout(userId);
    }

    /**
     * Get paginated list of groups with optional search keyword
     *
     * @return group list and some other info
     */
    @GetMapping("/groups")
    @Operation(summary = "Get Group List", description = "Retrieve a paginated list of groups with optional search keyword")
    public Result getGroupList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {

        return adminService.getGroupList(page, size, keyword);
    }

    /**
     * Disband a group by group ID
     *
     * @return success or fail
     */
    @PostMapping("/group/disband")
    @Operation(summary = "Disband Group", description = "Disband a group by group ID")
    public Result disbandGroup(String groupId) {
        log.info("Disband Group: {}", groupId);
        GroupInfo groupInfo = groupInfoRepository.findById(groupId).orElseThrow(() -> new IllegalArgumentException(Constants.GROUP_NOT_FOUND));
        //It is unnecessary to pass the group owner ID, but I want to reuse the existing service method in some method.
        return adminService.disbandGroup(groupInfo.getGroupOwnerId(), groupId);
    }

    /**
     * Get all system global configurations
     *
     * @return system settings
     */
    @GetMapping("/settings")
    @Operation(summary = "Get System Settings", description = "Retrieve all system global configurations")
    public Result getSystemSettings() {
        return adminService.getSystemSettings();
    }

    /**
     * Update robot configuration such as name, avatar, and welcome message
     *
     * @return success or fail
     */
    @PutMapping("/robot-config")
    @Operation(summary = "Update Robot Config", description = "Update robot name, avatar and welcome message")
    public Result updateRobotConfig(@RequestBody RobotConfigDTO robotConfigDTO) {
        return adminService.updateRobotConfig(robotConfigDTO);
    }

    /**
     * Send broadcast message to all users via the Robot
     *
     * @return success or fail
     */
    @PostMapping("/broadcast/send")
    @Operation(summary = "Send Broadcast", description = "Send a message (Text/Media) to ALL users via the Robot")
    public Result sendBroadcast(@RequestBody com.dbt.chatease.DTO.BroadcastDTO dto) {
        return adminService.sendBroadcast(dto);
    }

    /**
     * Get robot configuration (name, avatar, welcome message)
     *
     * @return robot configuration
     */
    @GetMapping("/robot-config")
    @Operation(summary = "Get Robot Config", description = "Retrieve current robot name, avatar and welcome message")
    public Result getRobotConfig() {
        return adminService.getRobotConfig();
    }

    /**
     * Get dashboard statistics such as total user count and group count
     *
     * @return dashboard stats
     */
    @GetMapping("/dashboard/stats")
    @Operation(summary = "Get Dashboard Stats", description = "Retrieve total user count and group count")
    public Result getDashboardStats() {
        return adminService.getDashboardStats();
    }

    /**
     * Get paginated list of broadcast history
     *
     * @return broadcast list
     */
    @GetMapping("/broadcast/list")
    @Operation(summary = "Get Broadcast List", description = "Retrieve a paginated list of system broadcasts")
    public Result getBroadcastList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return adminService.getBroadcastList(page, size);
    }

}