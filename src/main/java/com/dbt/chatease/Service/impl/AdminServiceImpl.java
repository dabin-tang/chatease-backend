package com.dbt.chatease.Service.impl;

import com.dbt.chatease.DTO.BroadcastDTO;
import com.dbt.chatease.DTO.RobotConfigDTO;
import com.dbt.chatease.Entity.*;
import com.dbt.chatease.Repository.*;
import com.dbt.chatease.Service.AdminService;
import com.dbt.chatease.Service.ChatService;
import com.dbt.chatease.Utils.Constants;
import com.dbt.chatease.Utils.Result;
import com.dbt.chatease.VO.GroupListVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final UserInfoRepository userInfoRepository;
    private final GroupInfoRepository groupInfoRepository;
    private final UserContactRepository userContactRepository;
    private final SysSettingRepository sysSettingRepository;
    private final ChatService chatService;
    private final SysBroadcastRepository sysBroadcastRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public Result getUserList(Integer page, Integer size, String keyword) {
        log.info("Get user list. Page: {}, Size: {}, Keyword: {}", page, size, keyword);

        //Create Pageable object (Sort by createTime descending)
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));

        Page<UserInfo> userPage = userInfoRepository.searchUsers(keyword, pageable);

        Map<String, Object> result = new HashMap<>();
        result.put("records", userPage.getContent());
        result.put("total", userPage.getTotalElements());
        result.put("current", page);
        result.put("size", size);
        result.put("pages", userPage.getTotalPages());

        return Result.ok(result);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateUserStatus(String userId, Integer status) {
        log.info("Admin updating user status. UserId: {}, Status: {}", userId, status);
        //validation
        if (userId == null || status == null) {
            return Result.fail(Constants.USER_ID_AND_STATUS_CANNOT_BE_NULL);
        }
        if (status != 0 && status != 1) {
            return Result.fail(Constants.INVALID_STATUS);
        }
        //Update status
        int rows = userInfoRepository.updateUserStatus(userId, status);
        if (rows > 0) {
            //TODO: If banning a user (status=0), force logout them(Done)
            String redisKey = "BANNED:" + userId;
            if (status == 0) {
                redisTemplate.opsForValue().set(redisKey, "true", 3650, java.util.concurrent.TimeUnit.DAYS);
            } else {
                redisTemplate.delete(redisKey);
            }
            return Result.ok(Constants.USER_STATUS_UPDATED_SUCCESS);
        } else {
            return Result.fail(Constants.USER_NOT_FOUND_OR_UPDATE_FAILED);
        }
    }

    @Override
    public Result forceLogout(String userId) {
        boolean exists = userInfoRepository.existsById(userId);
        if (Boolean.FALSE.equals(exists)) {
            return Result.fail(Constants.USER_NOT_FOUND);
        }
        //TODO: Force logout(Done)
        String redisKey = "BANNED:" + userId;
        redisTemplate.opsForValue().set(redisKey, "forced_logout", 1, java.util.concurrent.TimeUnit.DAYS);
        return null;
    }

    @Override
    public Result getGroupList(Integer page, Integer size, String keyword) {
        log.info("Admin fetching group list. Page: {}, Size: {}, Keyword: {}", page, size, keyword);
        //Create Pageable object (Default sort by createTime DESC)
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<GroupInfo> groupPage = groupInfoRepository.searchGroups(keyword, pageable);

        //Convert to VO and additional info
        List<GroupListVO> voList = groupPage.getContent().stream().map(group -> {
            //Get group owner name
            String ownerName = "Unknown";
            if (group.getGroupOwnerId() != null) {
                ownerName = userInfoRepository.findById(group.getGroupOwnerId())
                        .map(UserInfo::getNickName)
                        .orElse("Unknown User");
            }
            //Get member count for the group
            Long count = userContactRepository.countByContactIdAndContactType(group.getGroupId(), 1);
            return GroupListVO.fromEntity(group, ownerName, count);
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("records", voList);
        result.put("total", groupPage.getTotalElements());
        result.put("current", page);
        result.put("size", size);
        result.put("pages", groupPage.getTotalPages());

        return Result.ok(result);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result disbandGroup(String groupOwnerId, String groupId) {
        //soft delete the group by setting status to 0 (disbanded)
        int updateCount = groupInfoRepository.updateGroupStatus(groupId, groupOwnerId, 0);
        if (updateCount != 1) {
            return Result.fail(Constants.GROUP_DISBAND_FAILED);
        }
        //set all UserContact entries related to this group to status 2 (deleted)
        int count = userContactRepository.updateStatusByContactId(
                groupId,
                1,
                2,
                LocalDateTime.now()
        );
        if (count < 0) {
            return Result.fail(Constants.GROUP_DISBAND_FAILED);
        }

        //TODO: sent ws message to notify group members about disbanding (Done)
        chatService.sendSystemMessage("SYSTEM", groupId, 1, "Group has been disbanded by Administrator");
        return Result.ok(Constants.GROUP_DISBANDED_SUCCESS);
    }

    @Override
    public Result getSystemSettings() {
        log.info("Admin is getting all system settings");
        List<SysSetting> settings = sysSettingRepository.findAll();
        return Result.ok(settings);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateRobotConfig(RobotConfigDTO dto) {
        log.info("Admin updating robot config: {}", dto);
        //Update ROBOT_NICKNAME
        updateSingleSetting("ROBOT_NICKNAME", dto.getNickname());
        //Update ROBOT_AVATAR
        updateSingleSetting("ROBOT_AVATAR", dto.getAvatar());
        //Update ROBOT_WELCOME
        updateSingleSetting("ROBOT_WELCOME", dto.getWelcomeMessage());
        return Result.ok("Robot configuration updated successfully");
    }

    private void updateSingleSetting(String key, String value) {
        //Only update if value is provided (not null)
        if (value != null) {
            SysSetting setting = sysSettingRepository.findById(key).orElse(new SysSetting());
            setting.setSettingCode(key);
            setting.setSettingValue(value);
            //Don't overwrite description if it exists
            if (setting.getDescription() == null) {
                setting.setDescription("Updated by Admin");
            }
            sysSettingRepository.save(setting);
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result sendBroadcast(BroadcastDTO dto) {
        log.info("Admin sending broadcast: {}", dto);
        //Get Robot ID (Sender)
        String robotUid = "UID_ROBOT_001"; //Default ID
        var setting = sysSettingRepository.findById("ROBOT_UID");
        if (setting.isPresent()) {
            robotUid = setting.get().getSettingValue();
        }
        //Save Broadcast
        SysBroadcast broadcast = new SysBroadcast();
        broadcast.setSenderId(robotUid);
        broadcast.setContent(dto.getContent());
        broadcast.setMessageType(dto.getMessageType()); //Save type
        broadcast.setFilePath(dto.getFilePath());       //Save file path
        broadcast.setCreateTime(LocalDateTime.now());
        sysBroadcastRepository.save(broadcast);
        return Result.ok("Broadcast sent successfully");
    }


}