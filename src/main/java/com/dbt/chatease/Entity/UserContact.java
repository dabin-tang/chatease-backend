package com.dbt.chatease.Entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_contact")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@IdClass(UserContact.UserContactId.class)
@Schema(name = "UserContact", description = "User contact information entity")
public class UserContact implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID, User ID
     */
    @Id
    @Column(name = "user_id", length = 12, nullable = false)
    @Schema(description = "User ID", example = "USR123456789")
    private String userId;

    /**
     * 联系人ID或者群组ID, Contact ID or Group ID
     */
    @Id
    @Column(name = "contact_id", length = 12, nullable = false)
    @Schema(description = "Contact ID or Group ID", example = "USR987654321")
    private String contactId;

    /**
     * 联系人类型 0:好友 1:群组, Contact type: 0-Friend, 1-Group
     */
    @Column(name = "contact_type")
    @Schema(description = "Contact type: 0-Friend, 1-Group", example = "0")
    private Integer contactType;

    /**
     * 创建时间, Creation time
     */
    @Column(name = "create_time")
    @Schema(description = "Creation time", example = "2025-09-06T10:00:00")
    private LocalDateTime createTime;

    /**
     * 状态: 0:非好友 1:好友 2:已删除好友 3:被好友删除 4:已拉黑好友 5:被好友拉黑
     * Status: 0: Not a friend 1: Friend 2: Friend already deleted
     * 3: Deleted by friend 4: Friend already blocked 5: Blocked by friend
     */
    @Column(name = "status")
    @Schema(description = "Status: 0-Not friend, 1-Friend, 2-Deleted, 3-Blocked", example = "1")
    private Integer status = 0;

    /**
     * 最后更新时间, Last update time
     */
    @Column(name = "last_update_time")
    @Schema(description = "Last update time", example = "2025-09-06T12:00:00")
    private LocalDateTime lastUpdateTime;

    /**
     * 复合主键类
     * Composite primary key class
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserContactId implements Serializable {
        private static final long serialVersionUID = 1L;

        private String userId;
        private String contactId;
    }

}