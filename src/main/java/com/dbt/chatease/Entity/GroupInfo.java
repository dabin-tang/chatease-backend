package com.dbt.chatease.Entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "GroupInfo", description = "Group information entity")
public class GroupInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 群ID, Group ID
     */
    @Id
    @Column(name = "group_id", length = 12, nullable = false)
    @Schema(description = "Group ID", example = "GRP123456789")
    private String groupId;

    /**
     * 群组名, Group name
     */
    @Column(name = "group_name", length = 20)
    @Schema(description = "Group name", example = "Development Team")
    private String groupName;

    /**
     * 群主id, Group owner ID
     */
    @Column(name = "group_owner_id", length = 12)
    @Schema(description = "Group owner ID", example = "USR123456789")
    private String groupOwnerId;

    /**
     * 群头像, Group avatar
     */
    @Column(name = "group_avatar", length = 255)
    @Schema(description = "Group avatar URL", example = "http://example.com/group-avatar.jpg")
    private String groupAvatar;

    /**
     * 创建时间, Creation time
     */
    @Column(name = "create_time")
    @Schema(description = "Creation time", example = "2025-09-06T10:00:00")
    private LocalDateTime createTime;

    /**
     * 群公告, Group announcement
     */
    @Column(name = "group_notice", length = 500)
    @Schema(description = "Group announcement", example = "Welcome to our group! Please read the rules.")
    private String groupNotice;

    /**
     * 加入方式 0:直接加入 1:管理员同意后加入, Join type: 0-Join directly, 1-Join after admin approval
     */
    @Column(name = "join_type")
    @Schema(description = "Join type: 0-Join directly, 1-Join after admin approval", example = "0")
    private Integer joinType;

    /**
     * 状态 1:正常 0:解散, Status: 1-Active, 0-Disbanded
     */
    @Column(name = "status")
    @Schema(description = "Status: 1-Active, 0-Disbanded", example = "1")
    private Integer status = 1;
}