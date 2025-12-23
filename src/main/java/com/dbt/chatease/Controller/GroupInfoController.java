package com.dbt.chatease.Controller;

import com.dbt.chatease.DTO.GroupInfoDTO;
import com.dbt.chatease.DTO.GroupMemberOpDTO;
import com.dbt.chatease.Entity.GroupInfo;
import com.dbt.chatease.Service.GroupInfoService;
import com.dbt.chatease.Utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/group-info")
@Tag(name = "GroupInfo Controller", description = "Group Management APIs")
@RequiredArgsConstructor
@Validated
@Slf4j
public class GroupInfoController {
    private final GroupInfoService groupInfoService;

    /**
     * Create a new group and add the group owner as a member of the group
     *
     * @param groupInfoDTO Group information
     * @return success or fail
     */
    @PostMapping("/create")
    @Operation(summary = "Create Group and add the owner as a member",
            description = "Create a new group with the specified info and add the creator as the group owner and first member.")
    public Result createGroup(@RequestBody GroupInfoDTO groupInfoDTO) {
        log.info("Create Group: {}", groupInfoDTO);
        return groupInfoService.createGroup(groupInfoDTO);
    }

    /**
     * Update group information
     *
     * @param groupInfoDTO Group information to update
     * @return success or fail
     */
    @PutMapping("/update")
    @Operation(summary = "Update Group", description = "Update group information")
    public Result updateGroup(@RequestBody GroupInfoDTO groupInfoDTO) {
        log.info("Update Group: {}", groupInfoDTO);
        return groupInfoService.updateGroup(groupInfoDTO);
    }

    /**
     * Get groups the current user is a member of
     *
     * @return list of groups
     */
    @GetMapping("/my-groups")
    @Operation(summary = "Get My Groups", description = "Get the list of groups that the current user has joined.")
    public Result getMyGroups() {
        log.info("Get My Groups");
        return groupInfoService.getMyGroups();
    }

    /**
     * Get detailed information about a group by its ID, including the number of members.
     *
     * @param groupId ID of the group
     * @return group information and group member count
     */
    @GetMapping("/{groupId}")
    @Operation(summary = "Get Group Info by ID", description = "Get detailed information about a group by its ID, including the number of members.")
    public Result getGroupInfo(@PathVariable String groupId) {
        log.info("Get Group Info for ID: {}", groupId);
        return groupInfoService.getGroupInfoByIdAndMemberCount(groupId);
    }


    /**
     * Get group information along with its members by group ID
     *
     * @param groupId ID of the group
     * @return group information and list of members
     */
    @GetMapping("/members")
    @Operation(summary = "Get Group Info with Members by ID", description = "Get group information along with its members by group ID.")
    public Result getGroupInfoWithMembersByGroupId(@RequestParam String groupId) {
        log.info("Get Group Info with Members by ID: {}", groupId);
        return groupInfoService.getGroupInfoWithMembersByGroupId(groupId);
    }

    /**
     * Current user quits the group. If the user is the owner, ownership transfers to another member.
     *
     * @param groupId ID of the group to quit
     * @return success or fail
     */
    @PostMapping("/quit")
    @Operation(summary = "Quit Group", description = "Current user quits the group. If owner quits, ownership transfers.")
    public Result quitGroup(@RequestParam String groupId) {
        log.info("User quitting group: {}", groupId);
        return groupInfoService.quitGroup(groupId);
    }

    /**
     * Owner disbands the group.
     *
     * @param groupId ID of the group to disband
     * @return success or fail
     */
    @PostMapping("/disband")
    @Operation(summary = "Disband Group", description = "Owner disbands the group.")
    public Result disbandGroup(@RequestParam String groupId) {
        log.info("Owner disbanding group: {}", groupId);
        return groupInfoService.disbandGroup(groupId);
    }

    /**
     * Owner kicks members from the group.
     *
     * @param dto Data transfer object containing group ID and member IDs to kick
     * @return success or fail
     */
    @PostMapping("/kick")
    @Operation(summary = "Kick Members", description = "Owner removes members from group.")
    public Result kickMembers(@RequestBody GroupMemberOpDTO dto) {
        log.info("Kick members: {}", dto);
        return groupInfoService.kickMembers(dto);
    }

    /**
     * Add members to the group. Any member can invite others directly.
     *
     * @param dto Data transfer object containing group ID and member IDs to add
     * @return success or fail
     */
    @PostMapping("/add")
    @Operation(summary = "Add Members", description = "Any member can invite others directly.")
    public Result addMembers(@RequestBody GroupMemberOpDTO dto) {
        log.info("Add members: {}", dto);
        return groupInfoService.addMembers(dto);
    }

}