package com.dbt.chatease.Service.impl;

import com.dbt.chatease.DTO.UserApplyDTO;
import com.dbt.chatease.Entity.*;
import com.dbt.chatease.Exception.BusinessException;
import com.dbt.chatease.Handler.ChatWebSocketHandler;
import com.dbt.chatease.Repository.*;
import com.dbt.chatease.Service.UserApplyService;
import com.dbt.chatease.Service.UserContactService;
import com.dbt.chatease.Utils.Constants;
import com.dbt.chatease.Utils.Result;
import com.dbt.chatease.Utils.UserContext;
import com.dbt.chatease.VO.FriendRequestVO;
import com.dbt.chatease.VO.GroupRequestVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserApplyServiceImpl implements UserApplyService {
    private final UserContactRepository userContactRepository;
    private final UserInfoRepository userInfoRepository;
    private final GroupInfoRepository groupInfoRepository;
    private final UserApplyRepository userApplyRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatWebSocketHandler chatWebSocketHandler;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result sendRequest(UserApplyDTO userApplyDTO) {
        String applyUserId = UserContext.getCurrentUserId();
        String nickName = UserContext.getCurrentUser().getNickName();
        String contactId = userApplyDTO.getContactId();
        String defaultRequestMessage = "";
        if (userApplyDTO.getContactId().length() != 12) {
            throw new IllegalArgumentException(Constants.INVALID_CONTACT_ID);
        }
        //set default message if none provided
        if (!StringUtils.hasText(userApplyDTO.getApplyInfo())) {
            defaultRequestMessage = String.format("Hi, I am %s", nickName);
        }
        if (contactId.startsWith("UID")) {
            //Check if target user exists
            UserInfo targetUserInfo = userInfoRepository.findById(contactId).
                    orElseThrow(() -> new IllegalArgumentException(Constants.USER_NOT_FOUND));
            if (targetUserInfo.getStatus() == 0) {
                throw new BusinessException(Constants.USER_NOT_FOUND);
            }
            //Check if the target user is already a friend. Cannot add if blocked.
            UserContact reverseUserContact = userContactRepository.findByUserIdAndContactIdAndContactType(contactId, applyUserId, 0);
            if (reverseUserContact != null && reverseUserContact.getStatus() == 3) {
                throw new BusinessException(Constants.USER_HAS_BLOCKED_YOU);
            }
            //Check if the target user is already a friend(Abnormal user)
            UserContact userContactForApplyUser = userContactRepository.findByUserIdAndContactIdAndContactType(applyUserId, contactId, 0);
            if (userContactForApplyUser != null) {
                log.warn("Abnormal User {} attempted to add existing friend {}", applyUserId, contactId);
                throw new BusinessException(Constants.USER_ALREADY_ADDED);
            }
            //If not blocked and The target user can be added directly,add each other as friends
            if (targetUserInfo.getJoinType() == 0) {
                this.addBidirectionalFriend(applyUserId, contactId);

                //TODO: send ws message(Done)
                sendAndSaveMessage(applyUserId, contactId, 0, "I have added you as a friend. Let's chat!");
                return Result.ok(Constants.FRIEND_ADDED_SUCCESS);
            }
            //If not blocked by the target user
            //If target user ignore or rejected previous request and send a new request, update the existing request
            UserApply reApply = userApplyRepository.findByApplyUserIdAndReceiveUserIdAndContactIdAndContactType(applyUserId, contactId, contactId, 0);
            if (reApply != null) {
                reApply.setApplyInfo(StringUtils.hasText(userApplyDTO.getApplyInfo()) ? userApplyDTO.getApplyInfo() : defaultRequestMessage)
                        .setStatus(0) //Pending
                        .setLastApplyTime(System.currentTimeMillis());
                userApplyRepository.save(reApply);
                return Result.ok(Constants.REQUEST_SENT_SUCCESS);
            }
            //If It's first time sending request
            UserApply userApply = this.createUserApply(applyUserId, contactId, contactId, 0, StringUtils.hasText(userApplyDTO.getApplyInfo()) ? userApplyDTO.getApplyInfo() : defaultRequestMessage);
            //Save the friend request
            userApplyRepository.save(userApply);
        } else if (contactId.startsWith("GID")) {
            //Check if target group exists
            GroupInfo groupInfo = groupInfoRepository.findById(contactId).
                    orElseThrow(() -> new IllegalArgumentException(Constants.GROUP_NOT_FOUND));
            if (groupInfo.getStatus() == 0) {
                throw new BusinessException(Constants.GROUP_NOT_FOUND);
            }
            //Check if the user is already a member of the group
            UserContact userContactForApplyUser = userContactRepository.findByUserIdAndContactIdAndContactType(applyUserId, contactId, 1);
            if (userContactForApplyUser != null) {
                log.warn("Abnormal User {} attempted to join existing group {}", applyUserId, contactId);
                throw new BusinessException(Constants.GROUP_ALREADY_ADDED);
            }
            //If not a member and the group allows direct joining
            if (groupInfo.getJoinType() == 0) {
                UserContact existingGroupContact = userContactRepository.findByUserIdAndContactIdAndContactType(
                        applyUserId,    //current user ID
                        contactId,      //group ID
                        1               //Type: Group 1
                );

                if (existingGroupContact != null) {
                    //If exists, update the status to active
                    existingGroupContact.setStatus(1)
                            .setLastUpdateTime(LocalDateTime.now());
                    userContactRepository.save(existingGroupContact);
                } else {
                    //If not exists, create a new group membership
                    UserContact newUserContact = createUserContact(
                            applyUserId,   //current user ID
                            contactId,     //group id
                            1,             //Type: Group 1
                            1
                    );
                    userContactRepository.save(newUserContact);
                }

                //TODO: send ws message(Done)
                String joinMsg = String.format("%s joined the group", nickName);
                sendAndSaveMessage("SYSTEM", contactId, 1, joinMsg);
                return Result.ok(Constants.GROUP_JOIN_SUCCESS);
            }
            //If not a member
            //If ignore or rejected previous request and send a new request, update the existing request
            UserApply reApplyGroup = userApplyRepository.findByApplyUserIdAndReceiveUserIdAndContactIdAndContactType(applyUserId, groupInfo.getGroupOwnerId(), contactId, 1);
            if (reApplyGroup != null) {
                reApplyGroup.setApplyInfo(StringUtils.hasText(userApplyDTO.getApplyInfo()) ? userApplyDTO.getApplyInfo() : defaultRequestMessage)
                        .setStatus(0) //Pending
                        .setLastApplyTime(System.currentTimeMillis());
                userApplyRepository.save(reApplyGroup);
                return Result.ok(Constants.REQUEST_SENT_SUCCESS);
            }
            UserApply userApply = this.createUserApply(applyUserId, groupInfo.getGroupOwnerId(), contactId, 1, StringUtils.hasText(userApplyDTO.getApplyInfo()) ? userApplyDTO.getApplyInfo() : defaultRequestMessage);
            //Save the group join request
            userApplyRepository.save(userApply);
        } else {
            throw new IllegalArgumentException(Constants.INVALID_CONTACT_ID);
        }
        return Result.ok(Constants.REQUEST_SENT_SUCCESS);
    }

    @Override
    public Result getReceivedFriendRequests(Integer page, Integer pageSize) {
        String currentUserId = UserContext.getCurrentUserId();
        Pageable pageable = PageRequest.of(page - 1, pageSize);

        Page<FriendRequestVO> resultPage = userApplyRepository.findFriendRequestsWithUserInfo(currentUserId, pageable);
        return Result.ok(resultPage.getContent(), resultPage.getTotalElements());
    }

    @Override
    public Result getReceivedGroupRequests(Integer page, Integer pageSize) {
        String currentUserId = UserContext.getCurrentUserId();
        Pageable pageable = PageRequest.of(page - 1, pageSize);

        Page<GroupRequestVO> resultPage = userApplyRepository.findGroupRequestsWithInfo(currentUserId, pageable);
        return Result.ok(resultPage.getContent(), resultPage.getTotalElements());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result processApplyRequest(Integer applyId, Integer status) {
        String currentUserId = UserContext.getCurrentUserId();
        UserApply userApply = userApplyRepository.findById(applyId)
                .orElseThrow(() -> new IllegalArgumentException(Constants.APPLICATION_NOT_FOUND));
        //Only the receiver can process the request
        if (!userApply.getReceiveUserId().equals(currentUserId)) {
            throw new IllegalArgumentException(Constants.UNAUTHORIZED_OPERATION);
        }
        //Only pending requests can be processed
        if (userApply.getStatus() != 0) {
            throw new IllegalArgumentException(Constants.APPLY_REQUEST_ALREADY_PROCESSED);
        }
        if (status != 1 && status != 2 && status != 3) {
            throw new IllegalArgumentException(Constants.INVALID_STATUS);
        }
        //Update application status
        userApply.setStatus(status);
        userApplyRepository.save(userApply);
        //If rejected, do nothing and just return "Rejected"
        if (status == 2) {
            return Result.ok(Constants.APPLICATION_REJECTED_SUCCESS);
        }
        //If approved, add friend or join group
        if (status == 1) {
            if (userApply.getContactType() == 0) {
                //Add each other as friends(bidirectional friendship)
                this.addBidirectionalFriend(userApply.getApplyUserId(), userApply.getContactId());
                //TODO: send ws message(Done)
                sendAndSaveMessage(currentUserId, userApply.getApplyUserId(), 0, "I accepted your friend request. Let's chat!");
            } else if (userApply.getContactType() == 1) {
                UserContact newContact = createUserContact(
                        userApply.getApplyUserId(),
                        userApply.getContactId(),      //group id
                        1,                             //group type
                        1
                );
                userContactRepository.save(newContact);
                //TODO: send ws message(Done)
                String newMemberName = userInfoRepository.findById(userApply.getApplyUserId())
                        .map(UserInfo::getNickName).orElse("New Member");

                sendAndSaveMessage("SYSTEM", userApply.getContactId(), 1, newMemberName + " joined the group");
            }
            return Result.ok(Constants.APPLICATION_ACCEPTED_SUCCESS);
        } else if (status == 3) {
            //User blocks another user (only for friend requests, not group requests)
            if (userApply.getContactType() == 0) {
                //If blocked, update the contact status to blocked if exists, otherwise create a new blocked contact
                UserContact existingContact = userContactRepository.findByUserIdAndContactIdAndContactType(
                        userApply.getReceiveUserId(),  //current user id
                        userApply.getApplyUserId(),    //contact user id
                        0                              //Type: Friend 0
                );
                if (existingContact != null) {
                    existingContact.setStatus(3)
                            .setLastUpdateTime(LocalDateTime.now());
                    userContactRepository.save(existingContact);
                } else {
                    UserContact blockedContact = createUserContact(
                            userApply.getReceiveUserId(),
                            userApply.getApplyUserId(),
                            userApply.getContactType(), //Type: Friend 0
                            3
                    );
                    userContactRepository.save(blockedContact);
                }
                return Result.ok(Constants.APPLICATION_BLOCKED_SUCCESS);
            }
        }
        return Result.ok(Constants.APPLICATION_PROCESSED_SUCCESS);
    }

    //Create UserContact entity
    private UserContact createUserContact(String userId, String contactId,
                                          Integer contactType, Integer status) {
        return new UserContact()
                .setUserId(userId)
                .setContactId(contactId)
                .setContactType(contactType)
                .setStatus(status)
                .setCreateTime(LocalDateTime.now())
                .setLastUpdateTime(LocalDateTime.now());
    }

    //Create UserApply entity
    private UserApply createUserApply(String applyUserId, String receiveUserId,
                                      String contactId, Integer contactType, String applyMessage) {
        return new UserApply()
                .setApplyUserId(applyUserId)
                .setReceiveUserId(receiveUserId)
                .setContactId(contactId)
                .setContactType(contactType)
                .setStatus(0) //Pending
                .setApplyInfo(applyMessage)
                .setLastApplyTime(System.currentTimeMillis());
    }

    //Add each other as friends (bidirectional friendship)
    private void addBidirectionalFriend(String userIdA, String userIdB) {
        //Create or update the relationship A → B
        UserContact contactAB = userContactRepository.findByUserIdAndContactIdAndContactType(userIdA, userIdB, 0);
        if (contactAB == null) {
            contactAB = createUserContact(userIdA, userIdB, 0, 1);
        } else {
            contactAB.setStatus(1).setLastUpdateTime(LocalDateTime.now());
        }
        userContactRepository.save(contactAB);

        //Create or update the relationship B → A
        UserContact contactBA = userContactRepository.findByUserIdAndContactIdAndContactType(userIdB, userIdA, 0);
        if (contactBA == null) {
            contactBA = createUserContact(userIdB, userIdA, 0, 1);
        } else {
            contactBA.setStatus(1).setLastUpdateTime(LocalDateTime.now());
        }
        userContactRepository.save(contactBA);
    }


    /**
     * Send system/notification message and save to DB + Update Sessions
     * @param senderId Sender ID (or "SYSTEM")
     * @param receiverId Receiver ID (User ID or Group ID)
     * @param contactType 0: Personal, 1: Group
     * @param content Message content
     */
    private void sendAndSaveMessage(String senderId, String receiverId, Integer contactType, String content) {
        //Construct Message
        ChatMessage msg = new ChatMessage();
        String sessionId;
        if (contactType == 0) {
            String[] ids = {senderId, receiverId};
            Arrays.sort(ids);
            sessionId = ids[0] + "_" + ids[1];
        } else {
            sessionId = receiverId; // GroupID
        }

        //Personal Text (for welcome msg), 5: System Notification (for group join)
        int messageType = (contactType == 0) ? 0 : 5;
        msg.setSessionId(sessionId)
                .setSendUserId(senderId)
                .setContactId(receiverId)
                .setContactType(contactType)
                .setMessageType(messageType)
                .setContent(content)
                .setStatus(1) // Sent
                .setSendTime(System.currentTimeMillis());
        chatMessageRepository.save(msg);

        //Update Sessions & Push WebSocket
        if (contactType == 0) {
            //Personal Chat
            //Update both parties' session lists
            updateSessionForUser(senderId, receiverId, content, msg.getSendTime(), contactType);
            updateSessionForUser(receiverId, senderId, content, msg.getSendTime(), contactType);

            //Push to receiver
            chatWebSocketHandler.sendSystemNotification(receiverId, msg);

        } else {
            //Group Chat
            //Get ALL group members
            List<UserContact> members = userContactRepository.findByContactIdAndContactType(receiverId, 1);

            for (UserContact member : members) {
                String memberId = member.getUserId();

                //Update Session for EACH member
                updateSessionForUser(memberId, receiverId, content, msg.getSendTime(), contactType);

                //Push WebSocket to EACH member (except sender if it's a real user, but here we push to all for system msg)
                chatWebSocketHandler.sendSystemNotification(memberId, msg);
            }
        }
    }

    /**
     * update or create a ChatSession
     */
    private void updateSessionForUser(String userId, String contactId, String content, Long time, Integer type) {
        ChatSession session = chatSessionRepository.findByUserIdAndContactId(userId, contactId);
        if (session == null) {
            //Create new session if not exists
            session = new ChatSession();
            session.setUserId(userId);
            session.setContactId(contactId);
            session.setContactType(type);
            session.setUnreadCount(0); // Initial unread count

            //Fill basic info (Avatar/Name) to avoid blank session on UI
            if (type == 0) {
                //Get Friend Info
                var userOpt = userInfoRepository.findById(contactId);
                if (userOpt.isPresent()) {
                    session.setContactName(userOpt.get().getNickName());
                    session.setContactAvatar(userOpt.get().getAvatar());
                }
            } else {
                //Get Group Info
                var groupOpt = groupInfoRepository.findById(contactId);
                if (groupOpt.isPresent()) {
                    session.setContactName(groupOpt.get().getGroupName());
                    session.setContactAvatar(groupOpt.get().getGroupAvatar());
                }
            }
        }

        //Update latest message info
        session.setLastMessage(content);
        session.setLastReceiveTime(time);
        chatSessionRepository.save(session);
    }

}