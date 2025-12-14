package com.dbt.chatease.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_robot_relation")
@Accessors(chain = true)
public class UserRobotRelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", length = 12, nullable = false)
    private String userId;

    @Column(name = "robot_id", length = 50, nullable = false)
    private String robotId;

    /**
     * Status: 1-Normal, 0-Hidden/Removed, 2-Blocked
     */
    @Column(name = "status")
    private Integer status;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "last_read_time")
    private Long lastReadTime;
}