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
@Table(name = "user_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "UserInfo", description = "User information entity")
public class UserInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID, User ID
     */
    @Id
    @Column(name = "user_id", length = 12, nullable = false)
    @Schema(description = "User ID", example = "123456789012")
    private String userId;

    /**
     * 邮箱, Email
     */
    @Column(name = "email", length = 50, nullable = false, unique = true)
    @Schema(description = "Email", example = "user@example.com")
    private String email;

    /**
     * 昵称, Nickname
     */
    @Column(name = "nick_name", length = 20)
    @Schema(description = "Nickname", example = "John")
    private String nickName;

    /**
     * 用户头像URL, User avatar URL
     */
    @Column(name = "avatar", length = 255)
    @Schema(description = "User avatar URL", example = "http://example.com/avatar.jpg")
    private String avatar;

    /**
     * 加入方式 0:直接加入 1:同意后加好友, Join type: 0-Direct join, 1-Join after approval
     * Default is 1
     */
    @Column(name = "join_type")
    @Schema(description = "Join type: 0-Direct join, 1-Join after approval", example = "0")
    private Integer joinType;

    /**
     * 性别 0:女 1:男, Gender: 0-Female, 1-Male
     */
    @Column(name = "sex")
    @Schema(description = "Gender: 0-Female, 1-Male", example = "1")
    private Integer sex;

    /**
     * 密码, Password
     */
    @Column(name = "password", length = 60)
    @Schema(description = "Password", example = "encrypted_password")
    private String password;

    /**
     * 个性签名, Profile signature
     */
    @Column(name = "personal_signature", length = 50)
    @Schema(description = "Profile signature", example = "Hello World!")
    private String personalSignature;

    /**
     * 状态, Status (0: disabled, 1: active)
     */
    @Column(name = "status")
    @Schema(description = "Status", example = "1")
    private Integer status;

    /**
     * 创建时间, Creation time
     */
    @Column(name = "create_time")
    @Schema(description = "Creation time", example = "2025-09-06T10:00:00")
    private LocalDateTime createTime;

    /**
     * 最后登录时间, Last login time
     */
    @Column(name = "last_login_time")
    @Schema(description = "Last login time", example = "2025-09-06T12:00:00")
    private LocalDateTime lastLoginTime;

    /**
     * 地区名称, Area name
     */
    @Column(name = "area_name", length = 50)
    @Schema(description = "Area name", example = "Beijing")
    private String areaName;

    /**
     * 地区编号, Area code
     */
    @Column(name = "area_code", length = 50)
    @Schema(description = "Area code", example = "010")
    private String areaCode;

    /**
     * 最后离开时间, Last offline time
     */
    @Column(name = "last_off_time")
    @Schema(description = "Last offline time", example = "1725602400000")
    private Long lastOffTime;
}