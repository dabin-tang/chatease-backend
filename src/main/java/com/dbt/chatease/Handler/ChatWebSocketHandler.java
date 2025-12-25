package com.dbt.chatease.Handler;

import com.dbt.chatease.Entity.ChatMessage;
import com.dbt.chatease.Entity.ChatSession;
import com.dbt.chatease.Entity.GroupInfo;
import com.dbt.chatease.Entity.UserContact;
import com.dbt.chatease.Entity.UserInfo;
import com.dbt.chatease.Repository.*;
import com.dbt.chatease.Utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket Handler for processing chat messages
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final JwtUtil jwtUtil;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final UserContactRepository userContactRepository;
    private final ObjectMapper objectMapper;
    private final GroupInfoRepository groupInfoRepository;
    private final UserInfoRepository userInfoRepository;

    //Store online user sessions
    private static final Map<String, WebSocketSession> ONLINE_USERS = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket Connection : " + session.getRemoteAddress());
        String token = getTokenFromSession(session);
        if (token == null) {
            log.error("Token is null");
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }
        boolean isValid = jwtUtil.validateToken(token);
        if (!isValid) {
            log.error("Token invalid");
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }
        String userId = jwtUtil.getUserIdFromToken(token);
        ONLINE_USERS.put(userId, session);
        session.getAttributes().put("userId", userId);
        log.info("User Connected: {}", userId);
        //Notify friends that I am online
        notifyFriendsOnline(userId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("Received: {}", payload);
        String senderId = (String) session.getAttributes().get("userId");
        if (senderId == null) {
            log.error("Sender ID not found in session");
            return;
        }
        ChatMessage chatMessage;
        try {
            chatMessage = objectMapper.readValue(payload, ChatMessage.class);
        } catch (Exception e) {
            log.error("JSON Parse Error", e);
            return;
        }
        chatMessage.setSendUserId(senderId);
        UserInfo senderInfo = userInfoRepository.findById(senderId).orElse(null);
        if (senderInfo != null) {
            chatMessage.setSendUserAvatar(senderInfo.getAvatar());
            chatMessage.setSendUserNickName(senderInfo.getNickName());
        }
        String robotUid = "UID_ROBOT_001";
        log.info("Processing Message: Type={}, To={}", chatMessage.getContactType(), chatMessage.getContactId());
        //Security Checks
        if (chatMessage.getContactType() == 0) {
            //Personal Chat
            if (!chatMessage.getContactId().equals(robotUid)) {
                //Check friendship status (My side)
                UserContact senderContact = userContactRepository.findByUserIdAndContactIdAndContactType(
                        senderId, chatMessage.getContactId(), 0);
                if (senderContact == null || (senderContact.getStatus() != 1 && senderContact.getStatus() != 3)) {
                    // Status 1: Friend, 3: Blocked by me. If neither, it's invalid/deleted.
                    log.warn("Not friends or Deleted. Sender: {}, Receiver: {}", senderId, chatMessage.getContactId());
                    return;
                }

                //Check if I blocked them (Status 3) -> Fail
                if (senderContact.getStatus() == 3) {
                    log.warn("Message blocked: Sender {} has blocked Receiver {}", senderId, chatMessage.getContactId());

                    // Send Failure Message Back to Sender
                    chatMessage.setStatus(2); // Set status to 2 (Failed)
                    chatMessage.setSendTime(System.currentTimeMillis());
                    // Set specific error message for active block
                    chatMessage.setErrorMsg("You have blocked this user. Cannot send message.");

                    sendMessageToUser(senderId, new TextMessage(objectMapper.writeValueAsString(chatMessage)));
                    return; // Stop processing
                }

                //Check if They blocked me -> Fail
                UserContact receiverContact = userContactRepository.findByUserIdAndContactIdAndContactType(
                        chatMessage.getContactId(), senderId, 0);
                if (receiverContact != null && receiverContact.getStatus() == 3) {
                    log.warn("Message blocked: Receiver {} has blocked Sender {}", chatMessage.getContactId(), senderId);

                    //Send Failure Message Back to Sender
                    chatMessage.setStatus(2); // Set status to 2 (Failed)
                    chatMessage.setSendTime(System.currentTimeMillis());
                    //Set specific error message for passive block
                    chatMessage.setErrorMsg("You have been blocked by this user. Cannot send message.");

                    sendMessageToUser(senderId, new TextMessage(objectMapper.writeValueAsString(chatMessage)));
                    return; //Stop processing
                }
            }
        } else {
            //Group Chat
            UserContact member = userContactRepository.findByUserIdAndContactIdAndContactType(
                    senderId, chatMessage.getContactId(), 1);
            if (member == null || member.getStatus() != 1) {
                log.warn("Not group member.");
                return;
            }
        }
        //Generate Session ID
        if (chatMessage.getContactType() == 0) {
            String[] ids = {senderId, chatMessage.getContactId()};
            Arrays.sort(ids);
            chatMessage.setSessionId(ids[0] + "_" + ids[1]);
        } else {
            chatMessage.setSessionId(chatMessage.getContactId());
        }
        //Save to Database (Only for successful messages)
        chatMessage.setSendTime(System.currentTimeMillis());
        chatMessage.setStatus(1); // Default success status
        try {
            chatMessageRepository.save(chatMessage);
            updateChatSession(chatMessage);
        } catch (Exception e) {
            log.error("Error", e);
        }
        //Push Message
        TextMessage textMessage = new TextMessage(objectMapper.writeValueAsString(chatMessage));
        if (chatMessage.getContactType() == 0) {
            //Personal Chat
            if (!chatMessage.getContactId().equals(robotUid)) {
                sendMessageToUser(chatMessage.getContactId(), textMessage);
            }
            sendMessageToUser(senderId, textMessage);
        } else {
            //Group Chat
            List<UserContact> members = userContactRepository.findByContactIdAndContactType(chatMessage.getContactId(), 1);
            for (UserContact member : members) {
                sendMessageToUser(member.getUserId(), textMessage);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        if (userId != null) {
            ONLINE_USERS.remove(userId);
            //Notify friends that user is offline(hidden to user, but notify system)
            notifyFriendsOffline(userId);
        }
        log.info("User Disconnected: {}", userId);
    }


    private void updateChatSession(ChatMessage msg) {
        updateSingleSession(msg.getSendUserId(), msg.getContactId(), msg.getContent(), msg.getSendTime(), 0);
        if (msg.getContactType() == 0) {
            updateSingleSession(msg.getContactId(), msg.getSendUserId(), msg.getContent(), msg.getSendTime(), 1);
        } else {
            List<UserContact> members = userContactRepository.findByContactIdAndContactType(msg.getContactId(), 1);
            for (UserContact member : members) {
                if (!member.getUserId().equals(msg.getSendUserId())) {
                    updateSingleSession(member.getUserId(), msg.getContactId(), msg.getContent(), msg.getSendTime(), 1);
                }
            }
        }
    }

    private void updateSingleSession(String userId, String contactId, String content, Long time, int unreadIncrement) {
        try {
            ChatSession chatSession = chatSessionRepository.findByUserIdAndContactId(userId, contactId);
            if (chatSession == null) {
                chatSession = new ChatSession();
                chatSession.setUserId(userId);
                chatSession.setContactId(contactId);
                chatSession.setUnreadCount(0);
                if (contactId.startsWith("GID")) {
                    chatSession.setContactType(1);
                    chatSession.setSessionId(contactId);
                    var groupOpt = groupInfoRepository.findById(contactId);
                    if (groupOpt.isPresent()) {
                        chatSession.setContactName(groupOpt.get().getGroupName());
                        chatSession.setContactAvatar(groupOpt.get().getGroupAvatar());
                    }
                } else {
                    chatSession.setContactType(0);
                    String[] ids = {userId, contactId};
                    java.util.Arrays.sort(ids);
                    chatSession.setSessionId(ids[0] + "_" + ids[1]);
                    var userOpt = userInfoRepository.findById(contactId);
                    if (userOpt.isPresent()) {
                        chatSession.setContactName(userOpt.get().getNickName());
                        chatSession.setContactAvatar(userOpt.get().getAvatar());
                    }
                }
                //Fallback name
                if (chatSession.getContactName() == null) chatSession.setContactName("User");
            }

            //For Mixed Message (Type 6)
            chatSession.setLastMessage(content);
            chatSession.setLastReceiveTime(time);
            if (unreadIncrement > 0) {
                chatSession.setUnreadCount(chatSession.getUnreadCount() + unreadIncrement);
            }
            chatSessionRepository.save(chatSession);
        } catch (Exception e) {
            log.error("Session update error", e);
        }
    }

    private void sendMessageToUser(String userId, TextMessage message) {
        WebSocketSession session = ONLINE_USERS.get(userId);
        if (session != null && session.isOpen()) {
            try {
                synchronized (session) {
                    session.sendMessage(message);
                }
            } catch (IOException e) {
                log.error("Send error: " + userId, e);
            }
        }
    }

    private String getTokenFromSession(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri != null && uri.getQuery() != null) {
            for (String param : uri.getQuery().split("&")) {
                if (param.startsWith("token=")) return param.substring(6);
            }
        }
        return null;
    }

    public void sendSystemNotification(String userId, Object messageObj) {
        try {
            String json = objectMapper.writeValueAsString(messageObj);
            sendMessageToUser(userId, new TextMessage(json));
        } catch (IOException e) {
            log.error("Failed to push system notification: {}", userId, e);
        }
    }

    public static boolean isOnline(String userId) {
        return ONLINE_USERS.containsKey(userId);
    }

    //notify friends
    private void notifyFriendsOnline(String userId) {
        //Find all friends of this user
        List<UserContact> friends = userContactRepository.findByUserIdAndContactType(userId, 0); // 0 = Friend
        //Construct notification message
        String jsonMessage = String.format("{\"messageType\": 9, \"contactId\": \"%s\", \"content\": \"online\"}", userId);
        TextMessage message = new TextMessage(jsonMessage);
        for (UserContact friend : friends) {
            //Only send to friends who are currently online
            if (isOnline(friend.getContactId())) {
                sendMessageToUser(friend.getContactId(), message);
            }
        }
    }

    //notify friends offline
    private void notifyFriendsOffline(String userId) {
        //Find all friends of this user
        List<UserContact> friends = userContactRepository.findByUserIdAndContactType(userId, 0); // 0 = Friend
        //Construct notification message
        String jsonMessage = String.format("{\"messageType\": 10, \"contactId\": \"%s\", \"content\": \"offline\"}", userId);
        TextMessage message = new TextMessage(jsonMessage);
        for (UserContact friend : friends) {
            //Only send to friends who are currently online
            if (isOnline(friend.getContactId())) {
                sendMessageToUser(friend.getContactId(), message);
            }
        }
    }

}