package com.dbt.chatease.Service.impl;

import com.dbt.chatease.DTO.GroupInfoDTO;
import com.dbt.chatease.DTO.GroupMemberOpDTO;
import com.dbt.chatease.Repository.UserInfoRepository;
import com.dbt.chatease.Service.ChatService;
import com.dbt.chatease.VO.GroupInfoVO;
import com.dbt.chatease.DTO.GroupMemberDTO;
import com.dbt.chatease.Entity.GroupInfo;
import com.dbt.chatease.Entity.UserContact;
import com.dbt.chatease.Repository.GroupInfoRepository;
import com.dbt.chatease.Repository.UserContactRepository;
import com.dbt.chatease.Service.GroupInfoService;
import com.dbt.chatease.Utils.Constants;
import com.dbt.chatease.Utils.GroupIdGenerator;
import com.dbt.chatease.Utils.Result;
import com.dbt.chatease.Utils.UserContext;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class GroupInfoServiceImpl implements GroupInfoService {
    private final GroupInfoRepository groupInfoRepository;
    private final GroupIdGenerator groupIdGenerator;
    private final UserContactRepository userContactRepository;
    private final UserInfoRepository userInfoRepository;
    private final ChatService chatService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result createGroup(GroupInfoDTO groupInfoDTO) {
        log.info("Creating group with info: {}", groupInfoDTO);
        if (groupInfoDTO.getGroupId() != null) {
            throw new IllegalArgumentException(Constants.UNKNOWN_ERROR);
        }
        if (groupInfoDTO.getGroupName() == null || groupInfoDTO.getGroupName().isEmpty()) {
            throw new IllegalArgumentException(Constants.GROUP_NAME_EMPTY);
        }
        if (groupInfoDTO.getGroupName().length() > 12) {
            throw new IllegalArgumentException(Constants.GROUP_NAME_TOO_LONG);
        }
        if (groupInfoDTO.getGroupNotice().length() > 500) {
            throw new IllegalArgumentException(Constants.GROUP_NOTICE_TOO_LONG);
        }
        if (groupInfoDTO.getJoinType() == null) {
            throw new IllegalArgumentException(Constants.GROUP_JOIN_TYPE_EMPTY);
        }
        //set the required fields
        GroupInfo groupInfo = new GroupInfo();
        BeanUtils.copyProperties(groupInfoDTO, groupInfo);
        String groupId = groupIdGenerator.generate();
        String currentUserId = UserContext.getCurrentUserId();
        groupInfo.setGroupId(groupId)
                .setGroupOwnerId(currentUserId)
                .setCreateTime(LocalDateTime.now())
                .setStatus(1); // Active
        groupInfoRepository.save(groupInfo);

        //Add the group owner as a member of the group
        UserContact ownerContact = new UserContact();
        ownerContact.setUserId(currentUserId)
                .setContactId(groupId)
                .setContactType(1) // Group
                .setCreateTime(LocalDateTime.now())
                .setStatus(1)   // Active
                .setLastUpdateTime(LocalDateTime.now());
        userContactRepository.save(ownerContact);

        return Result.ok(Constants.GROUP_CREATE_SUCCESS);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateGroup(GroupInfoDTO groupInfoDTO) {
        log.info("Updating group with info: {}", groupInfoDTO);
        if (groupInfoDTO.getGroupId() == null || groupInfoDTO.getGroupId().isEmpty()) {
            throw new IllegalArgumentException(Constants.UNKNOWN_ERROR);
        }
        //Only group owner can update group info
        String currentUserId = UserContext.getCurrentUserId();
        GroupInfo groupInfo = groupInfoRepository.findById(groupInfoDTO.getGroupId()).
                orElseThrow(() -> new IllegalArgumentException(Constants.GROUP_NOT_FOUND));
        if (!groupInfo.getGroupOwnerId().equals(currentUserId)) {
            return Result.fail(Constants.ONLY_GROUP_OWNER_CAN_MODIFY);
        }
        //update all fields except groupId
        groupInfoRepository.updateGroupInfo(groupInfoDTO);

        //TODO: Send a WebSocket message on group name update.
        return Result.ok(Constants.GROUPINFO_SUCCESS_UPDATE);
    }

    @Override
    public Result getMyGroups() {
        //Get current user ID
        String currentUserId = UserContext.getCurrentUserId();
        //Get groups where the user is a member
        List<GroupInfo> groups = groupInfoRepository.findGroupsByUserId(currentUserId);
        return Result.ok(groups);
    }

    public GroupInfo getGroupInfo(String groupId) {
        String currentUserId = UserContext.getCurrentUserId();
        Optional<UserContact> byUserIdAndContactId = userContactRepository.findByUserIdAndContactId(currentUserId, groupId);
        if (byUserIdAndContactId.isEmpty() || byUserIdAndContactId.get().getContactType() != 1) {
            throw new IllegalArgumentException(Constants.GROUP_ACCESS_DENIED);
        }
        GroupInfo groupInfo = groupInfoRepository.findById(groupId).
                orElseThrow(() -> new IllegalArgumentException(Constants.GROUP_NOT_FOUND));
        return groupInfo;
    }

    @Override
    public Result getGroupInfoByIdAndMemberCount(String groupId) {
        GroupInfo groupInfo = getGroupInfo(groupId);
        Long groupMemberCount = userContactRepository.countByContactIdAndContactType(groupId, 1);
        return Result.ok(groupInfo, groupMemberCount);
    }

    @Override
    public Result getGroupInfoWithMembersByGroupId(String groupId) {
        GroupInfo groupInfo = this.getGroupInfo(groupId);
        List<GroupMemberDTO> members = userContactRepository.findGroupMembersWithUserInfo(groupId);
        GroupInfoVO groupInfoVO = new GroupInfoVO(groupInfo, members);
        return Result.ok(groupInfoVO);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result quitGroup(String groupId) {
        String currentUserId = UserContext.getCurrentUserId();
        //Check Group
        GroupInfo group = groupInfoRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException(Constants.GROUP_NOT_FOUND));
        //Check Membership
        UserContact myContact = userContactRepository.findByUserIdAndContactIdAndContactType(currentUserId, groupId, 1);
        if (myContact == null || myContact.getStatus() != 1) {
            return Result.fail("You are not a member of this group");
        }
        //If I am the group owner
        if (group.getGroupOwnerId().equals(currentUserId)) {
            //Find the earliest member to inherit ownership
            List<UserContact> candidates = userContactRepository.findEarliestMembers(
                    groupId, currentUserId, PageRequest.of(0, 1));
            if (candidates.isEmpty()) {
                //I am the last person -> Disband the group
                return this.disbandGroup(groupId);
            } else {
                //Transfer ownership
                UserContact successor = candidates.get(0);
                group.setGroupOwnerId(successor.getUserId());
                groupInfoRepository.save(group);

                // TODO: Send System Message: "Owner transferred to xxx"(Done)
                chatService.sendSystemMessage("SYSTEM", groupId, 1, "Owner transferred to " + successor.getUserId());
            }
        }
        //If I am not the group owner
        //Quit (Soft delete contact)
        myContact.setStatus(2); // 2: Deleted/Quit
        myContact.setLastUpdateTime(LocalDateTime.now());
        userContactRepository.save(myContact);

        // TODO: Send System Message: "xxx left the group"(Done)
        String myName = userInfoRepository.findById(currentUserId).get().getNickName();
        chatService.sendSystemMessage("SYSTEM", groupId, 1, myName + " left the group");
        return Result.ok("Successfully left the group");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result disbandGroup(String groupId) {
        String currentUserId = UserContext.getCurrentUserId();

        //Check Permission (Must be owner)
        GroupInfo group = groupInfoRepository.findById(groupId).orElse(null);
        if (group == null) return Result.fail(Constants.GROUP_NOT_FOUND);

        if (!group.getGroupOwnerId().equals(currentUserId)) {
            return Result.fail(Constants.ONLY_GROUP_OWNER_CAN_MODIFY);
        }
        //Set Group Status to 0 (Disbanded)
        group.setStatus(0);
        groupInfoRepository.save(group);
        //Set All Members Status to 2 (Deleted) or 4 (Disbanded specific status)
        userContactRepository.updateStatusByContactId(groupId, 1, 2, LocalDateTime.now());
        // TODO: Send System Message: "Group disbanded"(Done)
        chatService.sendSystemMessage("SYSTEM", groupId, 1, "Group has been disbanded");
        return Result.ok(Constants.GROUP_DISBANDED_SUCCESS);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result kickMembers(GroupMemberOpDTO dto) {
        String currentUserId = UserContext.getCurrentUserId();
        String groupId = dto.getGroupId();
        //Check Permission
        GroupInfo group = groupInfoRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException(Constants.GROUP_NOT_FOUND));
        if (!group.getGroupOwnerId().equals(currentUserId)) {
            return Result.fail(Constants.ONLY_GROUP_OWNER_CAN_MODIFY);
        }
        //Kick Loop
        for (String targetUserId : dto.getTargetUserIds()) {
            if (targetUserId.equals(currentUserId)) continue; //Can't kick self

            UserContact targetContact = userContactRepository.findByUserIdAndContactIdAndContactType(targetUserId, groupId, 1);
            if (targetContact != null && targetContact.getStatus() == 1) {
                targetContact.setStatus(3); //Blocked/Kicked (Removed by admin)
                targetContact.setLastUpdateTime(LocalDateTime.now());
                userContactRepository.save(targetContact);
            }
        }

        return Result.ok("Members removed successfully");
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result addMembers(GroupMemberOpDTO dto) {
        String currentUserId = UserContext.getCurrentUserId();
        String groupId = dto.getGroupId();

        //Check if I am a member
        boolean amIMember = userContactRepository.existsByUserIdAndContactIdAndContactType(currentUserId, groupId, 1);
        if (!amIMember) {
            return Result.fail(Constants.GROUP_ACCESS_DENIED);
        }

        GroupInfo group = groupInfoRepository.findById(groupId).orElseThrow();
        if (group.getStatus() == 0) return Result.fail("Group is disbanded");

        for (String targetUserId : dto.getTargetUserIds()) {
            //Check if user exists
            if (!userInfoRepository.existsById(targetUserId)) continue;

            UserContact targetContact = userContactRepository.findByUserIdAndContactIdAndContactType(targetUserId, groupId, 1);

            if (targetContact != null) {
                //Already was a member (maybe quit before), restore status
                if (targetContact.getStatus() != 1) {
                    targetContact.setStatus(1);
                    targetContact.setLastUpdateTime(LocalDateTime.now());
                    userContactRepository.save(targetContact);
                }
            } else {
                //New member
                UserContact newContact = new UserContact()
                        .setUserId(targetUserId)
                        .setContactId(groupId)
                        .setContactType(1)
                        .setStatus(1)
                        .setCreateTime(LocalDateTime.now())
                        .setLastUpdateTime(LocalDateTime.now());
                userContactRepository.save(newContact);
            }

            //TODO: Send System Message: "xxx invited yyy to the group"(Done)
            String addedName = userInfoRepository.findById(targetUserId).get().getNickName();
            String inviterName = userInfoRepository.findById(currentUserId).get().getNickName();
            chatService.sendSystemMessage("SYSTEM", groupId, 1, inviterName + " invited " + addedName + " to the group");
        }

        return Result.ok("Members added successfully");
    }

}