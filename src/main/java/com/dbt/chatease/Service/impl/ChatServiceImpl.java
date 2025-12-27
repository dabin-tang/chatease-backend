package com.dbt.chatease.Service.impl;

import com.dbt.chatease.Entity.*;
import com.dbt.chatease.Handler.ChatWebSocketHandler;
import com.dbt.chatease.Repository.*;
import com.dbt.chatease.Service.ChatService;
import com.dbt.chatease.Utils.Result;
import com.dbt.chatease.Utils.UserContext;
import com.dbt.chatease.VO.ChatSessionVO;
import com.dbt.chatease.VO.MessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatWebSocketHandler chatWebSocketHandler;
    private final UserContactRepository userContactRepository;
    private final UserInfoRepository userInfoRepository;
    private final GroupInfoRepository groupInfoRepository;
    private final SysBroadcastRepository sysBroadcastRepository;
    private final SysSettingRepository sysSettingRepository;

    @Override
    public Result getMySessions() {
        String currentUserId = UserContext.getCurrentUserId();
        // Get Robot UID to identify if a session belongs to the System Robot
        String robotUid = "UID_ROBOT_001"; // Default
        var robotUidSetting = sysSettingRepository.findById("ROBOT_UID");
        if (robotUidSetting.isPresent()) {
            robotUid = robotUidSetting.get().getSettingValue();
        }
        final String finalRobotUid = robotUid;
        // Pre-fetch Robot Name
        String robotName = "System Robot";
        String robotAvatar = "";
        var nameSetting = sysSettingRepository.findById("ROBOT_NICKNAME");
        if (nameSetting.isPresent()) robotName = nameSetting.get().getSettingValue();
        var avatarSetting = sysSettingRepository.findById("ROBOT_AVATAR");
        if (avatarSetting.isPresent()) robotAvatar = avatarSetting.get().getSettingValue();
        // Get sessions
        List<ChatSession> sessions = chatSessionRepository.findByUserId(currentUserId);
        List<ChatSessionVO> sessionVOs = new ArrayList<>();
        if (sessions.isEmpty()) {
            return Result.ok(sessionVOs);
        }
        // Get latest avatars and names
        for (ChatSession session : sessions) {
            ChatSessionVO vo = new ChatSessionVO();
            BeanUtils.copyProperties(session, vo);

            if (session.getContactType() == 1) {
                // It's a Group: Fetch latest Group Info
                groupInfoRepository.findById(session.getContactId()).ifPresent(group -> {
                    vo.setContactName(group.getGroupName());   // Use latest name
                    vo.setContactAvatar(group.getGroupAvatar()); // Use latest avatar
                });
            } else {
                // It's a User or Robot
                if (session.getContactId().equals(finalRobotUid)) {
                    // [FIX] If contact is Robot, use info from SysSetting
                    vo.setContactName(robotName);
                    vo.setContactAvatar(robotAvatar);
                } else {
                    // Regular User: Get latest User Info
                    userInfoRepository.findById(session.getContactId()).ifPresent(user -> {
                        vo.setContactName(user.getNickName());      // Use latest nickname
                        vo.setContactAvatar(user.getAvatar());      // Use latest avatar
                    });
                }
            }
            sessionVOs.add(vo);
        }
        sessionVOs.sort((a, b) -> Long.compare(b.getLastReceiveTime(), a.getLastReceiveTime()));
        return Result.ok(sessionVOs);
    }


    @Override
    public Result getChatHistory(String contactId, Integer contactType, Long lastMessageId) {
        String currentUserId = UserContext.getCurrentUserId();
        //Get Robot UID
        String robotUid = "UID_ROBOT_001"; //Default
        var robotSetting = sysSettingRepository.findById("ROBOT_UID");
        if (robotSetting.isPresent()) {
            robotUid = robotSetting.get().getSettingValue();
        }
        //Determine Session ID
        String sessionId;
        if (contactType == 0) {
            String[] ids = {currentUserId, contactId};
            Arrays.sort(ids);
            sessionId = ids[0] + "_" + ids[1];
        } else {
            sessionId = contactId;
        }
        List<ChatMessage> privateMessages;
        if (lastMessageId == null) {
            //First page: Get latest 15 messages (Ordered by ID Desc)
            privateMessages = chatMessageRepository.findTop15BySessionIdOrderByMessageIdDesc(sessionId);
        } else {
            //Load more: Get 15 messages older than lastMessageId
            privateMessages = chatMessageRepository.findTop15BySessionIdAndMessageIdLessThanOrderByMessageIdDesc(sessionId, lastMessageId);
        }
        Collections.reverse(privateMessages);
        List<MessageVO> voList = privateMessages.stream().map(msg -> {
            MessageVO vo = new MessageVO();
            BeanUtils.copyProperties(msg, vo);
            vo.setIsMe(msg.getSendUserId().equals(currentUserId));
            return vo;
        }).collect(Collectors.toList());

        // Merge Robot Broadcasts & Fallback
        // Only load system broadcasts on the first page load to avoid duplication/performance issues on scroll
        if (contactId.equals(robotUid) && lastMessageId == null) {
            //Get all system broadcasts
            List<SysBroadcast> broadcasts = sysBroadcastRepository.findAll();
            for (SysBroadcast b : broadcasts) {
                MessageVO vo = new MessageVO();
                vo.setMessageId(-b.getBroadcastId()); //Negative ID for broadcasts
                vo.setSendUserId(robotUid);
                vo.setContent(b.getContent());
                vo.setMessageType(b.getMessageType());
                vo.setFilePath(b.getFilePath());
                vo.setSendTime(java.sql.Timestamp.valueOf(b.getCreateTime()).getTime());
                vo.setIsMe(false);
                voList.add(vo);
            }
            if (voList.isEmpty()) {
                MessageVO welcome = new MessageVO();
                welcome.setMessageId(-999L); // Dummy ID
                welcome.setSendUserId(robotUid);
                welcome.setContent("Hi! Welcome to ChatEase. I am your intelligent assistant.");
                welcome.setMessageType(0);
                welcome.setSendTime(System.currentTimeMillis());
                welcome.setIsMe(false);
                voList.add(welcome);
            }
            //Re-sort combined list (Broadcasts + DB messages)
            voList.sort((m1, m2) -> Long.compare(m1.getSendTime(), m2.getSendTime()));
        }
        return Result.ok(voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result markAsRead(String contactId) {
        String currentUserId = UserContext.getCurrentUserId();
        ChatSession session = chatSessionRepository.findByUserIdAndContactId(currentUserId, contactId);
        if (session != null) {
            session.setUnreadCount(0);
            chatSessionRepository.save(session);
        }
        return Result.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendSystemMessage(String senderId, String receiverId, Integer contactType, String content) {
        ChatMessage msg = new ChatMessage();
        String sessionId;
        if (contactType == 0) {
            String[] ids = {senderId, receiverId};
            Arrays.sort(ids);
            sessionId = ids[0] + "_" + ids[1];
        } else {
            sessionId = receiverId;
        }
        msg.setSessionId(sessionId)
                .setSendUserId(senderId)
                .setContactId(receiverId)
                .setContactType(contactType)
                .setMessageType(5)
                .setContent(content)
                .setStatus(1)
                .setSendTime(System.currentTimeMillis());
        chatMessageRepository.save(msg);
        if (contactType == 0) {
            updateSessionAndPush(receiverId, senderId, msg);
            updateSessionAndPush(senderId, receiverId, msg);
        } else {
            List<UserContact> members = userContactRepository.findByContactIdAndContactType(receiverId, 1);
            for (UserContact member : members) {
                //Only push to active members (Status == 1)
                if (member.getStatus() == 1) {
                    updateSessionAndPush(member.getUserId(), receiverId, msg);
                }
            }
        }
    }

    private void updateSessionAndPush(String userId, String contactId, ChatMessage msg) {
        String robotUid = "UID_ROBOT_001";
        var robotUidSetting = sysSettingRepository.findById("ROBOT_UID");
        if (robotUidSetting.isPresent()) {
            robotUid = robotUidSetting.get().getSettingValue();
        }
        ChatSession session = chatSessionRepository.findByUserIdAndContactId(userId, contactId);
        if (session == null) {
            session = new ChatSession();
            session.setUserId(userId);
            session.setContactId(contactId);
            session.setSessionId(msg.getSessionId());
            session.setContactType(msg.getContactType());
            session.setUnreadCount(0);
            if (msg.getContactType() == 0) {
                if (contactId.equals(robotUid)) {
                    var nameSetting = sysSettingRepository.findById("ROBOT_NICKNAME");
                    if (nameSetting.isPresent()) session.setContactName(nameSetting.get().getSettingValue());
                    var avatarSetting = sysSettingRepository.findById("ROBOT_AVATAR");
                    if (avatarSetting.isPresent()) session.setContactAvatar(avatarSetting.get().getSettingValue());
                } else {
                    var userOpt = userInfoRepository.findById(contactId);
                    if (userOpt.isPresent()) {
                        session.setContactName(userOpt.get().getNickName());
                        session.setContactAvatar(userOpt.get().getAvatar());
                    }
                }
            } else {
                var groupOpt = groupInfoRepository.findById(contactId);
                if (groupOpt.isPresent()) {
                    session.setContactName(groupOpt.get().getGroupName());
                    session.setContactAvatar(groupOpt.get().getGroupAvatar());
                }
            }
        }
        session.setLastMessage(msg.getContent());
        session.setLastReceiveTime(msg.getSendTime());
        chatSessionRepository.save(session);
        chatWebSocketHandler.sendSystemNotification(userId, msg);
    }

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void pushBroadcastToUsersAsync(SysBroadcast broadcast) {
        long startTime = System.currentTimeMillis();
        log.info("Start distributing broadcast asynchronously. ID: {}", broadcast.getBroadcastId());
        //Fetch Robot UID from Settings
        String robotUid = "UID_ROBOT_001"; // Default
        var robotUidSetting = sysSettingRepository.findById("ROBOT_UID");
        if (robotUidSetting.isPresent()) {
            robotUid = robotUidSetting.get().getSettingValue();
        }
        final String finalRobotUid = robotUid;
        int page = 0;
        int batchSize = 500;
        boolean hasNext = true;
        String preview = broadcast.getMessageType() == 1 ? "[Image]" : broadcast.getContent();
        long now = System.currentTimeMillis();
        while (hasNext) {
            //Get a batch of users
            Page<UserInfo> userPage = userInfoRepository.findAll(PageRequest.of(page, batchSize));
            List<UserInfo> users = userPage.getContent();
            if (users.isEmpty()) {
                hasNext = false;
                break;
            }
            //Extract User IDs
            List<String> userIds = users.stream().map(UserInfo::getUserId).collect(Collectors.toList());
            //Bulk fetch EXISTING sessions by Robot's Contact ID.
            List<ChatSession> existingSessions = chatSessionRepository.findByContactIdAndUserIdIn(finalRobotUid, userIds);
            //Update existing sessions ONLY (Do not create new ones)
            List<ChatSession> sessionsToSave = new ArrayList<>();
            for (ChatSession session : existingSessions) {
                session.setLastMessage(preview);
                session.setLastReceiveTime(now);
                session.setUnreadCount(session.getUnreadCount() == null ? 1 : session.getUnreadCount() + 1);
                sessionsToSave.add(session);
            }
            //Bulk Save
            if (!sessionsToSave.isEmpty()) {
                chatSessionRepository.saveAll(sessionsToSave);
                // Send WebSocket Notification
                for (ChatSession session : sessionsToSave) {
                    ChatMessage notifyMsg = new ChatMessage();
                    notifyMsg.setSessionId(session.getSessionId());
                    notifyMsg.setContactId(finalRobotUid);
                    notifyMsg.setContactType(0); // 0 = Personal/Robot
                    notifyMsg.setSendUserId(finalRobotUid);
                    notifyMsg.setContent(preview);
                    notifyMsg.setMessageType(broadcast.getMessageType());
                    notifyMsg.setSendTime(now);
                    notifyMsg.setStatus(1); // Success

                    chatWebSocketHandler.sendSystemNotification(session.getUserId(), notifyMsg);
                }
            }
            log.info("Broadcast batch {} processed. Updated {} sessions.", page, sessionsToSave.size());
            //Next page
            page++;
            if (!userPage.hasNext()) {
                hasNext = false;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
            }
        }
        long duration = System.currentTimeMillis() - startTime;
        log.info("Broadcast completed: {} ms", duration);
    }

}