package com.dbt.chatease.Controller;

import com.dbt.chatease.Service.ChatService;
import com.dbt.chatease.Utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
@Tag(name = "Chat Controller", description = "Chat and Message APIs")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;

    /**
     * Get Session List (Home Page)
     */
    @GetMapping("/sessions")
    @Operation(summary = "Get Session List", description = "Get the list of recent chats with unread counts")
    public Result getSessions() {
        return chatService.getMySessions();
    }

    /**
     * Get Chat History
     */
    @GetMapping("/history")
    @Operation(summary = "Get Chat History", description = "Get message history for a specific contact or group")
    public Result getHistory(@RequestParam String contactId, 
                             @RequestParam Integer contactType) {
        return chatService.getChatHistory(contactId, contactType);
    }

    /**
     * Mark as Read (Clear Red Dot)
     */
    @PutMapping("/read")
    @Operation(summary = "Mark as Read", description = "Clear unread count for a session")
    public Result markAsRead(@RequestParam String contactId) {
        return chatService.markAsRead(contactId);
    }

}