package com.dbt.chatease.Handler;

import com.dbt.chatease.Entity.ChatMessage;
import com.dbt.chatease.Entity.ChatSession;
import com.dbt.chatease.Entity.GroupInfo;
import com.dbt.chatease.Entity.UserContact;
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
        log.info("WebSocket attempt from: " + session.getRemoteAddress());

        String token = getTokenFromSession(session);
        log.info("Extracted Token: " + (token == null ? "NULL" : token.substring(0, 10) + "..."));

        if (token == null) {
            log.error("Token is null, closing session.");
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        boolean isValid = jwtUtil.validateToken(token);
        log.info("Token Validation Result: " + isValid);

        if (!isValid) {
            log.error("Token invalid, closing session.");
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        String userId = jwtUtil.getUserIdFromToken(token);
        ONLINE_USERS.put(userId, session);
        session.getAttributes().put("userId", userId);
        log.info("User connected successfully: {}", userId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("Received message: {}", payload);

        // Parse JSON to ChatMessage object
        ChatMessage chatMessage = objectMapper.readValue(payload, ChatMessage.class);
        String senderId = (String) session.getAttributes().get("userId");

        // Robot UID constant
        String robotUid = "UID_ROBOT_001";

        // ==========================================
        // 1. Security Checks
        // ==========================================
        if (chatMessage.getContactType() == 0) {
            // Personal Chat Security
            // If sending to Robot, SKIP friend check (allow "Tree Hole" mode)
            if (chatMessage.getContactId().equals(robotUid)) {
                log.info("User {} sent message to Robot (Tree Hole)", senderId);
            } else {
                // Normal User: Check if they are friends (Status must be 1)
                com.dbt.chatease.Entity.UserContact contact = userContactRepository.findByUserIdAndContactIdAndContactType(
                        senderId, chatMessage.getContactId(), 0);

                // Status 1 means active friend. If null, 0, 2 (deleted), 3 (blocked) -> Reject.
                if (contact == null || contact.getStatus() != 1) {
                    log.warn("Message rejected: Not friends. Sender: {}, Receiver: {}", senderId, chatMessage.getContactId());
                    return;
                }
            }
        } else {
            // Group Chat Security
            // Check if group exists and is active
            GroupInfo group = groupInfoRepository.findById(chatMessage.getContactId()).orElse(null);
            if (group == null || group.getStatus() == 0) {
                log.warn("Message rejected: Group disbanded or not found. Group: {}", chatMessage.getContactId());
                return;
            }

            // Check if sender is a group member
            com.dbt.chatease.Entity.UserContact member = userContactRepository.findByUserIdAndContactIdAndContactType(
                    senderId, chatMessage.getContactId(), 1);
            if (member == null || member.getStatus() != 1) {
                log.warn("Message rejected: Not a group member. Sender: {}, Group: {}", senderId, chatMessage.getContactId());
                return;
            }
        }

        // ==========================================
        // 2. Fix: Generate Session ID (Critical!)
        // ==========================================
        if (chatMessage.getContactType() == 0) {
            // Personal Chat: Sort IDs to ensure unique session key (e.g., "A_B" is same as "B_A")
            String[] ids = {senderId, chatMessage.getContactId()};
            java.util.Arrays.sort(ids);
            chatMessage.setSessionId(ids[0] + "_" + ids[1]);
        } else {
            // Group Chat: SessionID is just the GroupID
            chatMessage.setSessionId(chatMessage.getContactId());
        }

        // ==========================================
        // 3. Fill & Save
        // ==========================================
        chatMessage.setSendUserId(senderId);
        chatMessage.setSendTime(System.currentTimeMillis());
        chatMessage.setStatus(1); // Set as Sent

        // Now session_id is set, so this save will succeed
        chatMessageRepository.save(chatMessage);

        // Update ChatSession (For Recent Chat List & Unread Count)
        updateChatSession(chatMessage);

        // ==========================================
        // 4. Push to Receiver
        // ==========================================
        TextMessage textMessage = new TextMessage(objectMapper.writeValueAsString(chatMessage));

        if (chatMessage.getContactType() == 0) {
            // Personal Chat
            // Only send via WebSocket if receiver is NOT the robot
            if (!chatMessage.getContactId().equals(robotUid)) {
                sendMessageToUser(chatMessage.getContactId(), textMessage);
            }
        } else if (chatMessage.getContactType() == 1) {
            // Group Chat
            // Find all members in the group
            List<UserContact> members = userContactRepository.findByContactIdAndContactType(chatMessage.getContactId(), 1);
            for (UserContact member : members) {
                // Don't send to self (sender already has optimistic update)
                if (!member.getUserId().equals(senderId)) {
                    sendMessageToUser(member.getUserId(), textMessage);
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        if (userId != null) {
            ONLINE_USERS.remove(userId);
        }
    }

    //Helper Methods

    /**
     * Update session for both sender and receiver(s)
     */
    private void updateChatSession(ChatMessage msg) {
        //1. Update Sender's session (No unread increment)
        updateSingleSession(msg.getSendUserId(), msg.getContactId(), msg.getContent(), msg.getSendTime(), 0);

        if (msg.getContactType() == 0) {
            //2. Personal: Update Receiver's session (Unread +1)
            updateSingleSession(msg.getContactId(), msg.getSendUserId(), msg.getContent(), msg.getSendTime(), 1);
        } else {
            //3. Group: Update All Members' sessions
            List<UserContact> members = userContactRepository.findByContactIdAndContactType(msg.getContactId(), 1);
            for (UserContact member : members) {
                if (!member.getUserId().equals(msg.getSendUserId())) {
                    //userId=Member, contactId=GroupId
                    updateSingleSession(member.getUserId(), msg.getContactId(), msg.getContent(), msg.getSendTime(), 1);
                }
            }
        }
    }

    private void updateSingleSession(String userId, String contactId, String content, Long time, int unreadIncrement) {
        // 1. Try to find an existing session
        ChatSession chatSession = chatSessionRepository.findByUserIdAndContactId(userId, contactId);

        // 2. If session does not exist, create a new one
        if (chatSession == null) {
            chatSession = new ChatSession();
            chatSession.setUserId(userId);
            chatSession.setContactId(contactId);
            chatSession.setUnreadCount(0); // Initialize unread count

            // 3. Determine contact type, fill info, and GENERATE SESSION ID
            if (contactId.startsWith("GID")) {
                chatSession.setContactType(1); // Group

                // Fix: Set Session ID for Group (SessionID = GroupID)
                chatSession.setSessionId(contactId);

                // Fetch Group Info to fill name/avatar
                var groupOpt = groupInfoRepository.findById(contactId);
                if (groupOpt.isPresent()) {
                    chatSession.setContactName(groupOpt.get().getGroupName());
                    chatSession.setContactAvatar(groupOpt.get().getGroupAvatar());
                }
            } else {
                chatSession.setContactType(0); // Personal

                // Fix: Set Session ID for Personal Chat (Sorted user IDs to ensure consistency)
                String[] ids = {userId, contactId};
                java.util.Arrays.sort(ids);
                chatSession.setSessionId(ids[0] + "_" + ids[1]);

                // Fetch User Info to fill name/avatar
                var userOpt = userInfoRepository.findById(contactId);
                if (userOpt.isPresent()) {
                    chatSession.setContactName(userOpt.get().getNickName());
                    chatSession.setContactAvatar(userOpt.get().getAvatar());
                }
            }
        }

        // 4. Update the latest message content and time
        chatSession.setLastMessage(content);
        chatSession.setLastReceiveTime(time);

        // 5. Increment unread count if needed
        if (unreadIncrement > 0) {
            chatSession.setUnreadCount(chatSession.getUnreadCount() + unreadIncrement);
        }

        // 6. Save to database
        chatSessionRepository.save(chatSession);
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
        log.info("WebSocket URI: " + uri);

        if (uri != null && uri.getQuery() != null) {
            String[] params = uri.getQuery().split("&");
            for (String param : params) {
                if (param.startsWith("token=")) {
                    return param.substring(6);
                }
            }
        }
        return null;
    }

    public void sendSystemNotification(String userId, Object messageObj) {
        try {
            String json = objectMapper.writeValueAsString(messageObj);
            sendMessageToUser(userId, new TextMessage(json));
        } catch (IOException e) {
            log.error("Failed to push system notification to user: {}", userId, e);
        }
    }


}