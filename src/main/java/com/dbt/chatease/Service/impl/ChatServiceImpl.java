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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
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
        List<ChatSession> sessions = chatSessionRepository.findByUserIdOrderByLastReceiveTimeDesc(currentUserId);

        List<ChatSessionVO> voList = sessions.stream().map(session -> {
            ChatSessionVO vo = new ChatSessionVO();
            BeanUtils.copyProperties(session, vo);
            return vo;
        }).collect(Collectors.toList());

        //Ensure Robot Session Exists
        String robotUid = sysSettingRepository.findById("ROBOT_UID")
                .map(SysSetting::getSettingValue).orElse("UID_ROBOT_001");
        boolean hasRobot = voList.stream().anyMatch(s -> s.getContactId().equals(robotUid));

        if (!hasRobot) {
            String robotNick = sysSettingRepository.findById("ROBOT_NICKNAME")
                    .map(SysSetting::getSettingValue).orElse("ChatEase Helper");
            String robotAvatar = sysSettingRepository.findById("ROBOT_AVATAR")
                    .map(SysSetting::getSettingValue).orElse("https://api.dicebear.com/7.x/bottts/png?seed=default");

            SysBroadcast latestBroadcast = sysBroadcastRepository.findTopByOrderByCreateTimeDesc();

            ChatSessionVO robotSession = new ChatSessionVO();
            robotSession.setSessionId(robotUid);
            robotSession.setContactId(robotUid);
            robotSession.setContactName(robotNick);
            robotSession.setContactAvatar(robotAvatar);
            robotSession.setContactType(0);
            robotSession.setUnreadCount(0);

            if (latestBroadcast != null) {
                robotSession.setLastMessage(latestBroadcast.getContent());
                robotSession.setLastReceiveTime(java.sql.Timestamp.valueOf(latestBroadcast.getCreateTime()).getTime());
            } else {
                robotSession.setLastMessage("Welcome to ChatEase!");
                robotSession.setLastReceiveTime(System.currentTimeMillis());
            }
            voList.add(0, robotSession);
        }

        return Result.ok(voList);
    }

    @Override
    public Result getChatHistory(String contactId, Integer contactType) {
        String currentUserId = UserContext.getCurrentUserId();

        //Get Robot UID
        String robotUid = "UID_ROBOT_001"; //Default
        var robotSetting = sysSettingRepository.findById("ROBOT_UID");
        if (robotSetting.isPresent()) {
            robotUid = robotSetting.get().getSettingValue();
        }

        //Get Private Messages (chat_message table)
        String sessionId;
        if (contactType == 0) {
            String[] ids = {currentUserId, contactId};
            Arrays.sort(ids);
            sessionId = ids[0] + "_" + ids[1];
        } else {
            sessionId = contactId;
        }

        List<ChatMessage> privateMessages = chatMessageRepository.findBySessionIdOrderBySendTimeAsc(sessionId);

        List<MessageVO> voList = privateMessages.stream().map(msg -> {
            MessageVO vo = new MessageVO();
            BeanUtils.copyProperties(msg, vo);
            vo.setIsMe(msg.getSendUserId().equals(currentUserId));
            return vo;
        }).collect(Collectors.toList());

        //Merge Robot Broadcasts & Fallback
        if (contactId.equals(robotUid)) {
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

            //Re-sort combined list
            voList.sort((m1, m2) -> m1.getSendTime().compareTo(m2.getSendTime()));
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
                updateSessionAndPush(member.getUserId(), receiverId, msg);
            }
        }
    }

    private void updateSessionAndPush(String userId, String contactId, ChatMessage msg) {
        ChatSession session = chatSessionRepository.findByUserIdAndContactId(userId, contactId);
        if (session == null) {
            session = new ChatSession();
            session.setUserId(userId);
            session.setContactId(contactId);
            session.setContactType(msg.getContactType());
            session.setUnreadCount(0);
            if (msg.getContactType() == 0) {
                var userOpt = userInfoRepository.findById(contactId);
                if (userOpt.isPresent()) {
                    session.setContactName(userOpt.get().getNickName());
                    session.setContactAvatar(userOpt.get().getAvatar());
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
}