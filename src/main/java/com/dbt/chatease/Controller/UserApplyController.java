package com.dbt.chatease.Controller;

import com.dbt.chatease.DTO.UserApplyDTO;
import com.dbt.chatease.Service.UserApplyService;
import com.dbt.chatease.Utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/user-apply")
@Tag(name = "UserApply Controller", description = "Connection Requests APIs")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserApplyController {
    private final UserApplyService userApplyService;

    /**
     * Send friend or group join request
     *
     * @return success or fail
     */
    @PostMapping("/send-request")
    @Operation(summary = "Send Friend or Group Join Request", description = "Send a friend request to a user or a join request to a group")
    public Result sendRequest(@RequestBody UserApplyDTO userApplyDTO) {
        log.info("Send Friend or Group Join Request: {}", userApplyDTO);
        return userApplyService.sendRequest(userApplyDTO);
    }

    /**
     * Get received friend requests
     *
     * @return list of received friend requests
     */
    @GetMapping("/received-friend-requests")
    @Operation(summary = "Get Received Friend Requests", description = "Get the list of friend requests received by the current user")
    public Result getReceivedFriendRequests(@RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("Get Received Friend Requests");
        return userApplyService.getReceivedFriendRequests(page, pageSize);
    }

    /**
     * Get received group join requests
     *
     * @return list of received group join requests
     */
    @GetMapping("/received-group-requests")
    @Operation(summary = "Get Received Group Requests", description = "Get the list of group join requests received by the current user")
    public Result getReceivedGroupRequests(@RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("Get Received Group Requests");
        return userApplyService.getReceivedGroupRequests(page, pageSize);
    }

    /**
     * Process friend or group join request
     *
     * @param applyId ID of the application request
     * @param status  Status to set (e.g., approved or rejected)
     * @return success or fail
     */
    @PutMapping("/process-apply-request")
    @Operation(summary = "Process Apply Request", description = "Approve or reject a friend or group join request")
    public Result processApplyRequest(@RequestParam Integer applyId, @RequestParam Integer status) {
        log.info("Process Apply Request: applyId={}, status={}", applyId, status);
        return userApplyService.processApplyRequest(applyId, status);
    }

}