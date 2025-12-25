package com.dbt.chatease.Controller;

import com.dbt.chatease.DTO.UserApplyDTO;
import com.dbt.chatease.Service.UserApplyService;
import com.dbt.chatease.Service.UserContactService;
import com.dbt.chatease.Service.UserInfoService;
import com.dbt.chatease.Utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/user-contact")
@Tag(name = "UserContact Controller", description = "Contact Management APIs")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserContactController {
    private final UserContactService userContactService;


    /**
     * Search for friends or Groups by user ID/group ID
     *
     * @return list of matching users
     */
    @GetMapping("/search")
    @Operation(summary = "Search for Friends or Groups", description = "Search for friends or groups by user ID or group ID")
    public Result searchFriendOrGroup(String contactId) {
        log.info("Search for Friends or Groups: {}", contactId);
        return userContactService.searchFriendOrGroup(contactId);
    }


    /**
     * Get contacts of the current user
     *
     * @return list of contacts
     */
    @GetMapping("/list")
    @Operation(summary = "Get My Contacts", description = "Get the list of contacts for the current user")
    public Result getMyContacts(@RequestParam Integer contactType) {
        log.info("Get My Contacts");
        return userContactService.getMyContacts(contactType);
    }

    /**
     * Get detailed information about a contact by contact ID
     *
     * @return contact details
     */
    @GetMapping("/contact-detail")
    @Operation(summary = "Get Contact Detail", description = "Get detailed information about a contact by contact ID")
    public Result getContactDetail(String contactId) {
        log.info("Get Contact Detail for ID: {}", contactId);
        return userContactService.getContactDetail(contactId);
    }

    /**
     * Delete a contact by contact ID
     *
     * @param contactId ID of the contact to delete
     * @return success or fail
     */
    @DeleteMapping("/delete-contact")
    @Operation(summary = "Delete Contact", description = "Delete a contact by contact ID")
    public Result deleteContact(String contactId) {
        log.info("Delete Contact with ID: {}", contactId);
        return userContactService.deleteContact(contactId);
    }

    /**
     * Block a contact by contact ID
     *
     * @param contactId ID of the contact to block
     * @return success or fail
     */
    @PostMapping("/block-contact")
    @Operation(summary = "Block Contact", description = "Block a contact by contact ID")
    public Result blockContact(String contactId) {
        log.info("Block Contact with ID: {}", contactId);
        return userContactService.blockContact(contactId);
    }

    /**
     * Unblock a contact by contact ID
     *
     * @param contactId ID of the contact to unblock
     * @return success or fail
     */
    @PostMapping("/unblock-contact")
    @Operation(summary = "Unblock Contact", description = "Unblock a contact by contact ID")
    public Result unblockContact(String contactId) {
        log.info("Unblock Contact with ID: {}", contactId);
        return userContactService.unblockContact(contactId);
    }

}