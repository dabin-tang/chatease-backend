package com.dbt.chatease.Service.impl;

import com.dbt.chatease.Entity.*;
import com.dbt.chatease.Repository.*;
import com.dbt.chatease.Service.UserContactService;
import com.dbt.chatease.Utils.Constants;
import com.dbt.chatease.Utils.Result;
import com.dbt.chatease.Utils.UserContext;
import com.dbt.chatease.VO.ContactVO;
import com.dbt.chatease.VO.GroupBasicVO;
import com.dbt.chatease.VO.UserInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserContactServiceImpl implements UserContactService {
    private final UserContactRepository userContactRepository;
    private final UserInfoRepository userInfoRepository;
    private final GroupInfoRepository groupInfoRepository;
    private final UserRobotRelationRepository userRobotRelationRepository;
    private final SysSettingRepository sysSettingRepository;


    @Override
    public Result searchFriendOrGroup(String contactId) {
        if (contactId == null || contactId.trim().isEmpty()) {
            return Result.fail(Constants.CONTACT_ID_EMPTY);
        }
        String currentUserId = UserContext.getCurrentUserId();
        //Check if contactId is a user or a group by its prefix
        if (contactId.startsWith("UID")) {
            UserInfo userInfo = userInfoRepository.findById(contactId).
                    orElseThrow(() -> new IllegalArgumentException(Constants.NO_MATCHING_CONTACT));

            UserInfoVO userInfoVO = new UserInfoVO();
            BeanUtils.copyProperties(userInfo, userInfoVO);

            //Check if user is searching for themselves
            if (contactId.equals(currentUserId)) {
                userInfoVO.setIsMe(true);
                userInfoVO.setIsFriend(null);
            } else {
                userInfoVO.setIsMe(false);
                boolean isFriend = userContactRepository.existsByUserIdAndContactIdAndContactType(currentUserId, contactId, 0);
                userInfoVO.setIsFriend(isFriend);
            }
            return Result.ok(userInfoVO);
        } else if (contactId.startsWith("GID")) {
            GroupInfo groupInfo = groupInfoRepository.findById(contactId).
                    orElseThrow(() -> new IllegalArgumentException(Constants.NO_MATCHING_CONTACT));
            boolean isMember = userContactRepository.existsByUserIdAndContactIdAndContactType(currentUserId, contactId, 1);
            GroupBasicVO groupBasicVO = new GroupBasicVO();
            BeanUtils.copyProperties(groupInfo, groupBasicVO);
            groupBasicVO.setIsMember(isMember);
            return Result.ok(groupBasicVO);
        } else {
            return Result.fail(Constants.INVALID_CONTACT_ID);
        }
    }

    @Override
    public Result getMyContacts(Integer contactType) {
        if (contactType == null || (contactType != 0 && contactType != 1)) {
            throw new IllegalArgumentException(Constants.INVALID_CONTACT_TYPE);
        }
        String currentUserId = UserContext.getCurrentUserId();
        //Get Friend Contacts
        if (Integer.valueOf(0).equals(contactType)) {
            //Get friend list
            List<ContactVO> friends = new ArrayList<>(userContactRepository.findFriendContacts(currentUserId));
            //Get Robot UID from settings
            String robotUid = sysSettingRepository.findById("ROBOT_UID")
                    .map(SysSetting::getSettingValue).orElse("UID_ROBOT_001");
            //Get existing relation
            UserRobotRelation relation = userRobotRelationRepository.findByUserIdAndRobotId(currentUserId, robotUid);

            if (relation == null) {
                log.info("Bug, Robot relation missing for user {},", currentUserId);
                relation = new UserRobotRelation()
                        .setUserId(currentUserId)
                        .setRobotId(robotUid)
                        .setStatus(1)
                        .setCreateTime(LocalDateTime.now())
                        .setLastReadTime(0L);
                try {
                    userRobotRelationRepository.save(relation);
                } catch (Exception e) {
                    log.error("Failed to repair robot relation", e);
                }
            }
            //Construct Robot VO and add to the top of the list
            if (relation != null) {
                String robotNick = sysSettingRepository.findById("ROBOT_NICKNAME")
                        .map(SysSetting::getSettingValue).orElse("ChatEase");
                String robotAvatar = sysSettingRepository.findById("ROBOT_AVATAR")
                        .map(SysSetting::getSettingValue).orElse("https://api.dicebear.com/7.x/bottts/png?seed=default");

                ContactVO robotVO = new ContactVO();
                robotVO.setNickName(robotNick);
                robotVO.setAvatar(robotAvatar);

                UserContact uc = new UserContact();
                uc.setUserId(currentUserId);
                uc.setContactId(robotUid);
                uc.setContactType(0);
                uc.setStatus(1);
                uc.setCreateTime(relation.getCreateTime());
                robotVO.setUserContact(uc);
                //Add Robot to the top
                friends.add(0, robotVO);
            }
            return Result.ok(friends);
        } else if (Integer.valueOf(1).equals(contactType)) {
            // Get Group Contacts
            List<ContactVO> groups = userContactRepository.findGroupContacts(currentUserId);
            return Result.ok(groups);
        }
        return Result.fail(Constants.UNKNOWN_ERROR);
    }

    @Override
    public Result getContactDetail(String contactId) {
        String currentUserId = UserContext.getCurrentUserId();
        this.validateFriendship(currentUserId, contactId);
        UserInfo userInfo = userInfoRepository.findById(contactId).
                orElseThrow(() -> new IllegalArgumentException(Constants.USER_NOT_FOUND));
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(userInfo, userInfoVO);
        userInfoVO.setIsFriend(true);
        return Result.ok(userInfoVO);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result deleteContact(String contactId) {
        String currentUserId = UserContext.getCurrentUserId();
        this.validateFriendship(currentUserId, contactId);
        userContactRepository.updateByUserIdAndContactIdAndContactType(currentUserId, contactId, 0, 2, LocalDateTime.now());
        userContactRepository.updateByUserIdAndContactIdAndContactType(contactId, currentUserId, 0, 2, LocalDateTime.now());
        return Result.ok(Constants.CONTACT_DELETED);
    }

    @Override
    public Result blockContact(String contactId) {
        String currentUserId = UserContext.getCurrentUserId();
        this.validateFriendship(currentUserId, contactId);
        userContactRepository.updateByUserIdAndContactIdAndContactType(currentUserId, contactId, 0, 3, LocalDateTime.now());
        return Result.ok(Constants.CONTACT_BLOCKED);
    }

    private void validateFriendship(String userId, String contactId) {
        boolean isFriend = userContactRepository.existsByUserIdAndContactIdAndContactType(userId, contactId, 0);
        if (Boolean.FALSE.equals(isFriend)) {
            throw new IllegalArgumentException(Constants.USER_NOT_FOUND);
        }
    }


}