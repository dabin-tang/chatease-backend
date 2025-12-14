package com.dbt.chatease.Entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Entity
@Table(name = "user_apply")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "UserApply", description = "User application entity")
public class UserApply implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 自增ID, Auto increment ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "apply_id")
    @Schema(description = "Auto increment ID", example = "1")
    private Integer applyId;

    /**
     * 申请人id, Applicant user ID
     */
    @Column(name = "apply_user_id", length = 12, nullable = false)
    @Schema(description = "Applicant user ID", example = "USR123456789")
    private String applyUserId;

    /**
     * 接收人ID, Receiver user ID
     */
    @Column(name = "receive_user_id", length = 12, nullable = false)
    @Schema(description = "Receiver user ID", example = "USR987654321")
    private String receiveUserId;

    /**
     * 联系人类型 0:好友 1:群组, Contact type: 0-Friend, 1-Group
     */
    @Column(name = "contact_type", nullable = false)
    @Schema(description = "Contact type: 0-Friend, 1-Group", example = "0")
    private Integer contactType;

    /**
     * 联系人群组ID, Contact or group ID
     */
    @Column(name = "contact_id", length = 12)
    @Schema(description = "Contact or group ID", example = "GRP123456789")
    private String contactId;

    /**
     * 最后申请时间, Last application time
     */
    @Column(name = "last_apply_time")
    @Schema(description = "Last application time", example = "1725602400000")
    private Long lastApplyTime;

    /**
     * 状态 0:待处理 1:已同意 2:已拒绝 3:已拉黑, Status: 0-Pending, 1-Accepted, 2-Rejected, 3-Blocked
     */
    @Column(name = "status")
    @Schema(description = "Status: 0-Pending, 1-Accepted, 2-Rejected, 3-Blocked", example = "0")
    private Integer status;

    /**
     * 申请信息, Application information
     */
    @Column(name = "apply_info", length = 100)
    @Schema(description = "Application information", example = "Hello, please accept my friend request!")
    private String applyInfo;
}